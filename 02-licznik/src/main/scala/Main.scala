import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

case object Increment
case object Decrement
case object Reset
case object GetCount

class LicznikActor extends Actor {
  private var count = 0

  def receive: Receive = {
    case Increment => count += 1
    case Decrement => count -= 1
    case Reset     => count = 0
    case GetCount  => sender() ! count
  }
}

object Main extends App {
  val system  = ActorSystem("licznik")
  val licznik = system.actorOf(Props[LicznikActor]())

  implicit val timeout: Timeout = Timeout(3.seconds)

  def odczyt() = Await.result(licznik ? GetCount, timeout.duration)

  licznik ! Increment
  licznik ! Increment
  licznik ! Increment
  licznik ! Decrement
  println(s"Aktualny licznik: ${odczyt()}")

  licznik ! Reset
  println(s"Po resecie: ${odczyt()}")

  system.terminate()
}
