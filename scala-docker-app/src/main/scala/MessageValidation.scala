/** Sprawdza tresc wiadomosci, zanim trafi do bazy.
  *
  * Osobny obiekt, bo Main przy starcie od razu laczy sie z baza. Tutaj nie
  * ma zadnych zaleznosci, wiec test dziala bez Postgresa.
  */
object MessageValidation {

  val MaxLength = 500

  /** Powod odrzucenia razem z kodem HTTP, ktory dostanie klient. */
  final case class Rejection(statusCode: Int, message: String)

  /** Zwraca tekst bez bialych znakow na brzegach albo powod odrzucenia.
    * Dlugosc liczona jest po obcieciu bialych znakow. */
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
