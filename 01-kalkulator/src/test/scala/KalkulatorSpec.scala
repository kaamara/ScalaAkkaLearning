import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class KalkulatorSpec
  extends TestKit(ActorSystem("test-system"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  // Kazdy test dostaje swiezego aktora, zeby testy nie wplywaly na siebie.
  // ImplicitSender sprawia, ze odpowiedz aktora trafia do testu,
  // a expectMsg czeka na nia i porownuje z oczekiwana.
  private def nowyKalkulator() = system.actorOf(Props[KalkulatorActor]())

  "KalkulatorActor" should {

    "odeslac sume przy Add" in {
      nowyKalkulator() ! Add(2, 3)
      expectMsg(Wynik(5))
    }

    "odeslac roznice przy Subtract" in {
      nowyKalkulator() ! Subtract(2, 5)
      expectMsg(Wynik(-3))
    }

    "odeslac iloczyn przy Multiply" in {
      nowyKalkulator() ! Multiply(4, -3)
      expectMsg(Wynik(-12))
    }

    "odeslac iloraz calkowity przy Divide" in {
      nowyKalkulator() ! Divide(7, 2)
      expectMsg(Wynik(3))
    }

    "odeslac blad przy dzieleniu przez zero" in {
      nowyKalkulator() ! Divide(1, 0)
      expectMsg(Blad("dzielenie przez zero"))
    }

    "liczyc dalej po bledzie" in {
      val kalkulator = nowyKalkulator()
      kalkulator ! Divide(1, 0)
      kalkulator ! Add(1, 1)
      expectMsg(Blad("dzielenie przez zero"))
      expectMsg(Wynik(2))
    }
  }
}
