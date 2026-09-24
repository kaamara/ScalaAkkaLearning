import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.duration._

sealed trait Operacja
case class Add(a: Int, b: Int)      extends Operacja
case class Subtract(a: Int, b: Int) extends Operacja
case class Multiply(a: Int, b: Int) extends Operacja
case class Divide(a: Int, b: Int)   extends Operacja

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

/** Zamienia linie wpisana przez uzytkownika na operacje.
  *
  * Osobno od petli w Main, zeby dalo sie to przetestowac bez klawiatury.
  * Zle wejscie daje None zamiast wyjatku - "abc + 5" albo liczba spoza
  * zakresu Int nie moga wywrocic programu.
  */
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
  val system     = ActorSystem("kalkulator-system")
  val kalkulator = system.actorOf(Props[KalkulatorActor](), "kalkulator")

  implicit val timeout: Timeout = Timeout(3.seconds)

  println("Operacje: +  -  *  /")
  println("Format: liczba operator liczba  (np. 10 + 5)")
  println("Wpisz 'exit' aby wyjsc")

  var running = true
  while (running) {
    // readLine zwraca null, gdy wejscie sie skonczy: Ctrl+D,
    // potok albo "docker run" bez -it. Traktujemy to jak "exit".
    Option(scala.io.StdIn.readLine("> ")).map(_.trim) match {
      case None | Some("exit") =>
        running = false
      case Some(line) =>
        Parser.parse(line) match {
          case None =>
            println("Nieprawidlowy format. Przyklad: 10 + 5")
          case Some(op) =>
            // Czekamy na odpowiedz aktora, zanim pokazemy kolejny prompt.
            Await.result((kalkulator ? op).mapTo[Odpowiedz], timeout.duration) match {
              case Wynik(value)  => println(s"Wynik: $value")
              case Blad(message) => println(s"Blad: $message")
            }
        }
    }
  }

  system.terminate()
}
