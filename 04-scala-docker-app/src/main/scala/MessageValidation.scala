object MessageValidation {

  val MaxLength = 500

  final case class Rejection(statusCode: Int, message: String)

  def validate(raw: String): Either[Rejection, String] = {
    val text = raw.trim
    if (text.isEmpty)
      Left(Rejection(400, "Pusta wiadomosc"))
    else if (text.length > MaxLength)
      Left(Rejection(413, s"Wiadomosc dluzsza niz $MaxLength znakow"))
    else
      Right(text)
  }
}
