import java.sql.{Connection, DriverManager}
import scala.util.Using

object Main extends cask.MainRoutes {

  override def host: String = "0.0.0.0"
  override def port: Int    = 8081

  override def debugMode: Boolean = false

  private def withConnection[T](body: Connection => T): T =
    Using.resource(DriverManager.getConnection(sys.env("DB_URL"), sys.env("DB_USER"), sys.env("DB_PASSWORD")))(body)

  withConnection(
    _.createStatement().execute("CREATE TABLE IF NOT EXISTS messages (id SERIAL PRIMARY KEY, content TEXT NOT NULL)")
  )

  @cask.get("/")
  def hello(): String = "Backend gotowy!"

  @cask.post("/add")
  def addMessage(req: cask.Request): cask.Response[String] =
    MessageValidation.validate(req.text()) match {
      case Left(rejection) =>
        cask.Response(rejection.message, statusCode = rejection.statusCode)
      case Right(text) =>
        withConnection { conn =>
          val insert = conn.prepareStatement("INSERT INTO messages (content) VALUES (?)")
          insert.setString(1, text)
          insert.executeUpdate()
        }
        cask.Response("Zapisano!")
    }

  @cask.get("/messages")
  def getMessages(): String = withConnection { conn =>
    val rows = conn.createStatement().executeQuery("SELECT content FROM messages ORDER BY id DESC LIMIT 5")
    Iterator.continually(rows).takeWhile(_.next()).map(row => s"- ${row.getString("content")}\n").mkString
  }

  initialize()
}
