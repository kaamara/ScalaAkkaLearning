import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await

case object Increment
case object Decrement
case object Reset
case object GetCount

class LicznikActor extends Actor {
  var count: Int = 0

  def receive: Receive = {
    case Increment => count += 1
    case Decrement => count -= 1
    case Reset     => count = 0
    case GetCount  => sender() ! count
  }
}

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("licznik-system")
  implicit val timeout: Timeout = Timeout(3.seconds)
  import system.dispatcher

  val licznik = system.actorOf(Props[LicznikActor], "licznik")

  licznik ! Increment
  licznik ! Increment
  licznik ! Increment
  licznik ! Decrement

  val future = licznik ? GetCount
  val result = Await.result(future, 3.seconds)
  println(s"Aktualny licznik: $result")

  licznik ! Reset

  val futureAfterReset = licznik ? GetCount
  val resultAfterReset = Await.result(futureAfterReset, 3.seconds)
  println(s"Po resecie: $resultAfterReset")

  system.terminate()
}