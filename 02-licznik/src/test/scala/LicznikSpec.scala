import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import akka.pattern.ask
import akka.util.Timeout
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import scala.concurrent.Await
import scala.concurrent.duration._

class LicznikSpec
  extends TestKit(ActorSystem("test-system"))
    with ImplicitSender
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  implicit val timeout: Timeout = Timeout(3.seconds)

  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "LicznikActor" should {

    "zwrocic 0 na starcie" in {
      val licznik = system.actorOf(akka.actor.Props[LicznikActor])
      val result = Await.result(licznik ? GetCount, 3.seconds)
      result shouldEqual 0
    }

    "zwiekszyc licznik po Increment" in {
      val licznik = system.actorOf(akka.actor.Props[LicznikActor])
      licznik ! Increment
      licznik ! Increment
      val result = Await.result(licznik ? GetCount, 3.seconds)
      result shouldEqual 2
    }

    "zmniejszyc licznik po Decrement" in {
      val licznik = system.actorOf(akka.actor.Props[LicznikActor])
      licznik ! Increment
      licznik ! Decrement
      val result = Await.result(licznik ? GetCount, 3.seconds)
      result shouldEqual 0
    }

    "zerowac licznik po Reset" in {
      val licznik = system.actorOf(akka.actor.Props[LicznikActor])
      licznik ! Increment
      licznik ! Increment
      licznik ! Reset
      val result = Await.result(licznik ? GetCount, 3.seconds)
      result shouldEqual 0
    }
  }
}