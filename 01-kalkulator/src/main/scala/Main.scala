import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

sealed trait Operacja
case class Add(a: Int, b: Int)      extends Operacja
case class Subtract(a: Int, b: Int) extends Operacja
case class Multiply(a: Int, b: Int) extends Operacja
case class Divide(a: Int, b: Int)   extends Operacja

case class Wynik(value: Long)
case class Blad(message: String)

class KalkulatorActor extends Actor {
  def receive: Receive = {
    case Add(a, b)      => sender() ! Wynik(a.toLong + b)
    case Subtract(a, b) => sender() ! Wynik(a.toLong - b)
    case Multiply(a, b) => sender() ! Wynik(a.toLong * b)
    case Divide(_, 0)   => sender() ! Blad("dzielenie przez zero")
    case Divide(a, b)   => sender() ! Wynik(a.toLong / b)
  }
}

object Parser {
  def parse(line: String): Option[Operacja] =
    line.trim.split("""\s+""") match {
      case Array(a, op, b) =>
        for {
          x <- a.toIntOption
          y <- b.toIntOption
          operacja <- op match {
            case "+" => Some(Add(x, y))
            case "-" => Some(Subtract(x, y))
            case "*" => Some(Multiply(x, y))
            case "/" => Some(Divide(x, y))
            case _   => None
          }
        } yield operacja
      case _ => None
    }
}

object Main extends App {
  val system     = ActorSystem("kalkulator")
  val kalkulator = system.actorOf(Props[KalkulatorActor]())

  implicit val timeout: Timeout = Timeout(3.seconds)

  println("Wpisz dzialanie, np. 10 + 5 (operatory: + - * /). Wyjscie: exit")

  Iterator
    .continually(StdIn.readLine("> "))
    .takeWhile(line => line != null && line.trim != "exit")
    .foreach { line =>
      Parser.parse(line) match {
        case None => println("Nieprawidlowy format. Przyklad: 10 + 5")
        case Some(operacja) =>
          Await.result(kalkulator ? operacja, timeout.duration) match {
            case Wynik(value)  => println(s"Wynik: $value")
            case Blad(message) => println(s"Blad: $message")
          }
      }
    }

  system.terminate()
}
