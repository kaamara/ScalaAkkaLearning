import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ParserSpec extends AnyWordSpec with Matchers {

  import Parser.parse

  "Parser.parse" should {

    "rozpoznac kazdy z czterech operatorow" in {
      parse("10 + 5") shouldBe Some(Add(10, 5))
      parse("10 - 5") shouldBe Some(Subtract(10, 5))
      parse("10 * 5") shouldBe Some(Multiply(10, 5))
      parse("10 / 5") shouldBe Some(Divide(10, 5))
    }

    "przyjac liczby ujemne" in {
      parse("-3 * 4") shouldBe Some(Multiply(-3, 4))
    }

    "zignorowac nadmiarowe spacje" in {
      parse("  10   +  5 ") shouldBe Some(Add(10, 5))
    }

    "zwrocic None zamiast wyjatku, gdy liczba nie jest liczba" in {
      parse("abc + 5") shouldBe None
      parse("10 + x") shouldBe None
    }

    "zwrocic None dla liczby spoza zakresu Int" in {
      parse("99999999999 + 1") shouldBe None
    }

    "zwrocic None dla nieznanego operatora" in {
      parse("10 % 5") shouldBe None
    }

    "zwrocic None dla zlego formatu" in {
      parse("") shouldBe None
      parse("10+5") shouldBe None
      parse("10 + 5 + 1") shouldBe None
    }
  }
}
