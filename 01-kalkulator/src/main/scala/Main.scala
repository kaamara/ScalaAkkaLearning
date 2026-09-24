import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

case class Add(a: Int, b: Int)
case class Subtract(a: Int, b: Int)
case class Multiply(a: Int, b: Int)
case class Divide(a: Int, b: Int)

// Odpowiedzi aktora. Aktor odsyla wynik nadawcy zamiast go wypisywac -
// dzieki temu test moze sprawdzic, co przyszlo.
sealed trait Odpowiedz
case class Wynik(value: Int)     extends Odpowiedz
case class Blad(message: String) extends Odpowiedz

class KalkulatorActor extends Actor {
  def receive: Receive = {
    case Add(a, b)      => sender() ! Wynik(a + b)
    case Subtract(a, b) => sender() ! Wynik(a - b)
    case Multiply(a, b) => sender() ! Wynik(a * b)
    case Divide(_, 0)   => sender() ! Blad("dzielenie przez zero")
    case Divide(a, b)   => sender() ! Wynik(a / b)
  }
}

object Main extends App {
  val system     = ActorSystem("kalkulator-system")
  val kalkulator = system.actorOf(Props[KalkulatorActor](), "kalkulator")

  implicit val timeout: Timeout = Timeout(3.seconds)

  println("Operacje: +  -  *  /")
  println("Format: liczba operator liczba  (np. 10 + 5)")
  println("Wpisz 'exit' aby wyjsc")

  var running = true
  while (running) {
    val input = scala.io.StdIn.readLine("> ")
    input.trim match {
      case "exit" => running = false
      case line =>
        val operacja = line.split(" ") match {
          case Array(a, "+", b) => Some(Add(a.toInt, b.toInt))
          case Array(a, "-", b) => Some(Subtract(a.toInt, b.toInt))
          case Array(a, "*", b) => Some(Multiply(a.toInt, b.toInt))
          case Array(a, "/", b) => Some(Divide(a.toInt, b.toInt))
          case _                => None
        }

        operacja match {
          case None =>
            println("Nieprawidlowy format. Przyklad: 10 + 5")
          case Some(op) =>
            Await.result((kalkulator ? op).mapTo[Odpowiedz], timeout.duration) match {
              case Wynik(value)  => println(s"Wynik: $value")
              case Blad(message) => println(s"Blad: $message")
            }
        }
    }
  }

  system.terminate()
}
