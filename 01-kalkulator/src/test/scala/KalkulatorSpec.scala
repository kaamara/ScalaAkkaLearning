import akka.actor.ActorSystem
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

  "KalkulatorActor" should {
    "wypisac wynik dodawania" in {
      val kalkulator = system.actorOf(akka.actor.Props[KalkulatorActor])
      kalkulator ! Add(2, 3)
      Thread.sleep(100)
    }
  }
}