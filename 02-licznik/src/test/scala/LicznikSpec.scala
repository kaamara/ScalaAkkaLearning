import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike

class LicznikSpec
  extends TestKit(ActorSystem("test"))
    with ImplicitSender
    with AnyWordSpecLike
    with BeforeAndAfterAll {

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  private def nowyLicznik() = system.actorOf(Props[LicznikActor]())

  "LicznikActor" should {

    "zwrocic 0 na starcie" in {
      nowyLicznik() ! GetCount
      expectMsg(0)
    }

    "zwiekszyc licznik po Increment" in {
      val licznik = nowyLicznik()
      licznik ! Increment
      licznik ! Increment
      licznik ! GetCount
      expectMsg(2)
    }

    "zmniejszyc licznik po Decrement" in {
      val licznik = nowyLicznik()
      licznik ! Decrement
      licznik ! GetCount
      expectMsg(-1)
    }

    "zerowac licznik po Reset" in {
      val licznik = nowyLicznik()
      licznik ! Increment
      licznik ! Increment
      licznik ! Reset
      licznik ! GetCount
      expectMsg(0)
    }
  }
}
