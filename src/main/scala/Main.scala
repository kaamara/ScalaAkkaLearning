import cask._
import java.sql.DriverManager

object Main extends cask.MainRoutes {
  val dbUrl = sys.env.getOrElse("DB_URL", "jdbc:postgresql://db:5432/mojabaza")
  val dbUser = sys.env.getOrElse("DB_USER", "user")
  val dbPass = sys.env.getOrElse("DB_PASSWORD", "password")

  def getConnection = {
    Class.forName("org.postgresql.Driver")
    DriverManager.getConnection(dbUrl, dbUser, dbPass)
  }

  def setupDatabase(): Unit = {
    val conn = getConnection
    val stmt = conn.createStatement()
    stmt.execute("CREATE TABLE IF NOT EXISTS messages (id SERIAL, content TEXT, ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")
    stmt.close()
    conn.close()
  }

  @cask.post("/add")
  def addMessage(req: cask.Request) = {
    val text = req.text()
    if (text.nonEmpty) {
      val conn = getConnection
      try {
        val ps = conn.prepareStatement("INSERT INTO messages (content) VALUES (?)")
        ps.setString(1, text)
        ps.executeUpdate()
      } finally { conn.close() }
    }
    cask.Response("Zapisano!", headers = Seq("Access-Control-Allow-Origin" -> "*"))
  }

  @cask.get("/messages")
  def getMessages() = {
    val conn = getConnection
    try {
      val rs = conn.createStatement().executeQuery("SELECT content FROM messages ORDER BY ts DESC LIMIT 5")
      var list = ""
      while(rs.next()) {
        list += "• " + rs.getString("content") + "\n"
      }
      cask.Response(data = list, headers = Seq("Access-Control-Allow-Origin" -> "*"))
    } finally { conn.close() }
  }

  @cask.get("/")
  def hello() = cask.Response("Backend gotowy!", headers = Seq("Access-Control-Allow-Origin" -> "*"))

  override def host: String = "0.0.0.0"
  override def port: Int = 8081

  setupDatabase()
  initialize()
}