import com.zaxxer.hikari.{HikariConfig, HikariDataSource}

import java.sql.Connection
import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

object Main extends cask.MainRoutes {

  private val MaxMessageLength = 500

  /** Czyta zmienna srodowiskowa albo od razu przerywa start.
    *
    * Celowo bez wartosci domyslnej dla hasla. Domyslne haslo znaczyloby,
    * ze zle ustawiona aplikacja wstaje jak gdyby nigdy nic i nikt tego
    * nie zauwaza. Lepiej, zeby nie wstala wcale i powiedziala dlaczego.
    */
  private def requiredEnv(name: String): String =
    sys.env.get(name).map(_.trim).filter(_.nonEmpty).getOrElse {
      throw new IllegalStateException(
        s"Brak zmiennej srodowiskowej $name. Aplikacja nie startuje bez jawnej konfiguracji."
      )
    }

  /** Czyta zmienna srodowiskowa albo bierze wartosc domyslna.
    * Dla ustawien, ktore nie sa tajne - host, port.
    */
  private def env(name: String, default: String): String =
    sys.env.get(name).map(_.trim).filter(_.nonEmpty).getOrElse(default)

  /** Pula gotowych polaczen do bazy.
    *
    * Otwieranie nowego polaczenia przy kazdym zapytaniu jest wolne i
    * szybko przepelnia limit bazy. Tutaj polaczenia sa otwarte raz
    * i pozyczane w kolko.
    */
  private val dataSource: HikariDataSource = {
    val config = new HikariConfig()
    config.setJdbcUrl(requiredEnv("DB_URL"))
    config.setUsername(requiredEnv("DB_USER"))
    config.setPassword(requiredEnv("DB_PASSWORD"))
    config.setMaximumPoolSize(10)
    config.setConnectionTimeout(5000)
    config.setPoolName("scala-docker-app")
    // Nazwa sterownika podana wprost. W jednym duzym pliku .jar
    // automatyczne wykrywanie potrafi zawiesc.
    config.setDriverClassName("org.postgresql.Driver")
    new HikariDataSource(config)
  }

  /** Pozycza polaczenie z puli i zawsze je oddaje.
    * Bez tego "finally" pula skonczylaby sie po 10 zapytaniach. */
  private def withConnection[T](body: Connection => T): T = {
    val conn = dataSource.getConnection
    try body(conn)
    finally conn.close()
  }

  /** Tworzy tabele, jesli jej nie ma.
    * Probuje kilka razy, bo baza po starcie potrzebuje chwili. */
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

  // Brak naglowkow CORS i tak ma byc. Strona i backend siedza pod tym
  // samym adresem (nginx), wiec nie sa potrzebne. Gdyby je dodac,
  // dowolna strona w internecie mogla by pisac do naszej bazy.

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

  // Adres i port ze zmiennych srodowiskowych, zeby dalo sie je zmienic
  // bez budowania obrazu od nowa.
  override def host: String = env("HTTP_HOST", "0.0.0.0")
  override def port: Int    = env("HTTP_PORT", "8081").toInt

  sys.addShutdownHook(dataSource.close())

  setupDatabase()
  initialize()
}
