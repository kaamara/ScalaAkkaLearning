import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

import java.sql.Connection
import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object Main extends cask.MainRoutes {

  private val MaxMessageLength = 500

  /** Sekret bez wartosci domyslnej.
    *
    * Zapis `sys.env.getOrElse("DB_PASSWORD", "password")` sprawia, ze zle
    * skonfigurowany kontener wstaje z dzialajacym haslem i nikt sie nie
    * orientuje. Lepiej nie wystartowac wcale niz wystartowac na domyslnym.
    *
    * Poswiadczen do bazy nie hashuje sie — sterownik JDBC musi wyslac je
    * doslownie. Chroni sie je zarzadzaniem sekretem: .env poza repo, dalej
    * docker secret zamontowany jako plik, docelowo Key Vault.
    */
  private def requiredEnv(name: String): String =
    sys.env.get(name).map(_.trim).filter(_.nonEmpty).getOrElse {
      throw new IllegalStateException(
        s"Brak zmiennej srodowiskowej $name. Aplikacja nie startuje bez jawnej konfiguracji."
      )
    }

  /** Pula polaczen.
    *
    * Poprzednia wersja wolala DriverManager.getConnection przy kazdym
    * zadaniu. Nawiazanie polaczenia z Postgresem to kilkadziesiat
    * milisekund i osobny proces po stronie serwera; przy domyslnym limicie
    * 100 polaczen wystarczy kilkadziesiat rownoleglych zadan, zeby baza
    * zaczela odrzucac nowe.
    */
  private val dataSource: HikariDataSource = {
    val config = new HikariConfig()
    config.setJdbcUrl(requiredEnv("DB_URL"))
    config.setUsername(requiredEnv("DB_USER"))
    config.setPassword(requiredEnv("DB_PASSWORD"))
    config.setMaximumPoolSize(10)
    config.setConnectionTimeout(5000)
    config.setPoolName("scala-docker-app")
    // Jawna klasa sterownika zamiast polegania na ServiceLoaderze.
    // W fat JAR-ze wystarczy zla strategia scalania META-INF/services,
    // zeby rejestracja sterownika zniknela i pula padla na starcie.
    config.setDriverClassName("org.postgresql.Driver")
    new HikariDataSource(config)
  }

  /** conn.close() na polaczeniu z puli nie zamyka socketu, tylko oddaje je
    * z powrotem. Bez tego finally pula wycieka i zatyka sie po 10 zadaniach. */
  private def withConnection[T](body: Connection => T): T = {
    val conn = dataSource.getConnection
    try body(conn)
    finally conn.close()
  }

  /** Retry, bo healthcheck compose'a mowi tylko, ze Postgres przyjmuje
    * polaczenia — nie, ze zdazyl odtworzyc dane z wolumenu. */
  @tailrec
  private def setupDatabase(attemptsLeft: Int = 10): Unit = {
    val attempt = Try {
      withConnection { conn =>
        val stmt = conn.createStatement()
        try stmt.execute(
          """CREATE TABLE IF NOT EXISTS messages (
            |  id      SERIAL PRIMARY KEY,
            |  content TEXT NOT NULL,
            |  ts      TIMESTAMPTZ NOT NULL DEFAULT now()
            |)""".stripMargin
        )
        finally stmt.close()
      }
    }

    attempt match {
      case Success(_) =>
        println("[init] schemat bazy gotowy")
      case Failure(e) if attemptsLeft > 1 =>
        println(s"[init] baza niedostepna (${e.getMessage}); ponawiam za 2s, prob zostalo ${attemptsLeft - 1}")
        Thread.sleep(2000)
        setupDatabase(attemptsLeft - 1)
      case Failure(e) =>
        throw new IllegalStateException("Nie udalo sie przygotowac schematu bazy", e)
    }
  }

  // Zadnych naglowkow Access-Control-*: nginx wystawia backend pod /api/
  // tego samego originu co frontend, wiec przegladarka nie widzi tu
  // zadania cross-origin. Wczesniejsze "Access-Control-Allow-Origin: *"
  // na endpoincie zapisujacym do bazy pozwalalo dowolnej stronie w
  // internecie wyslac POST w imieniu odwiedzajacego.

  @cask.post("/add")
  def addMessage(req: cask.Request): cask.Response[String] = {
    val text = req.text().trim

    if (text.isEmpty)
      cask.Response("Pusta wiadomosc", statusCode = 400)
    else if (text.length > MaxMessageLength)
      cask.Response(s"Wiadomosc dluzsza niz $MaxMessageLength znakow", statusCode = 413)
    else {
      withConnection { conn =>
        val ps = conn.prepareStatement("INSERT INTO messages (content) VALUES (?)")
        try {
          ps.setString(1, text)
          ps.executeUpdate()
        } finally ps.close()
      }
      cask.Response("Zapisano!")
    }
  }

  @cask.get("/messages")
  def getMessages(): cask.Response[String] = withConnection { conn =>
    val ps = conn.prepareStatement(
      "SELECT content FROM messages ORDER BY ts DESC, id DESC LIMIT 5"
    )
    try {
      val rs = ps.executeQuery()
      val out = new StringBuilder
      while (rs.next()) out.append("- ").append(rs.getString("content")).append('\n')
      cask.Response(out.toString)
    } finally ps.close()
  }

  @cask.get("/")
  def hello(): cask.Response[String] = cask.Response("Backend gotowy!")

  override def host: String = "0.0.0.0"
  override def port: Int = 8081

  sys.addShutdownHook(dataSource.close())

  setupDatabase()
  initialize()
}
