import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class MessageValidationSpec extends AnyWordSpec with Matchers {

  import MessageValidation._

  "MessageValidation.validate" should {

    "przepuscic zwykla wiadomosc" in {
      validate("czesc") shouldBe Right("czesc")
    }

    "obciac biale znaki z brzegow" in {
      validate("  czesc \n") shouldBe Right("czesc")
    }

    "odrzucic puste body kodem 400" in {
      validate("") shouldBe Left(Rejection(400, "Pusta wiadomosc"))
    }

    "odrzucic body z samych bialych znakow kodem 400" in {
      validate(" \t\r\n ") shouldBe Left(Rejection(400, "Pusta wiadomosc"))
    }

    "przepuscic wiadomosc o dlugosci dokladnie rownej limitowi" in {
      val text = "a" * MaxLength
      validate(text) shouldBe Right(text)
    }

    "odrzucic wiadomosc dluzsza o jeden znak kodem 413" in {
      validate("a" * (MaxLength + 1)) shouldBe
        Left(Rejection(413, s"Wiadomosc dluzsza niz $MaxLength znakow"))
    }

    "liczyc dlugosc dopiero po obcieciu bialych znakow" in {
      val text = "a" * MaxLength
      validate(s"   $text   ") shouldBe Right(text)
    }

    // Limit jest w znakach, nie w bajtach: "z" z kropka (0x017c) zajmuje
    // w UTF-8 dwa bajty, a liczy sie jako jeden znak.
    // Litera podana kodem, zeby plik zostal w samym ASCII.
    "liczyc polskie litery jako jeden znak" in {
      val text = 0x017c.toChar.toString * MaxLength
      validate(text) shouldBe Right(text)
    }
  }
}
