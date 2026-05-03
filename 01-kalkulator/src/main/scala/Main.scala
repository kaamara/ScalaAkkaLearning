import akka.actor.{Actor, ActorSystem, Props}

case class Add(a: Int, b: Int)
case class Subtract(a: Int, b: Int)
case class Multiply(a: Int, b: Int)
case class Divide(a: Int, b: Int)

class KalkulatorActor extends Actor {
  def receive: Receive = {
    case Add(a, b)      => println(s"Wynik: ${a + b}")
    case Subtract(a, b) => println(s"Wynik: ${a - b}")
    case Multiply(a, b) => println(s"Wynik: ${a * b}")
    case Divide(a, b)   =>
      if (b == 0) println("Blad: dzielenie przez zero")
      else println(s"Wynik: ${a / b}")
  }
}

object Main extends App {
  val system     = ActorSystem("kalkulator-system")
  val kalkulator = system.actorOf(Props[KalkulatorActor], "kalkulator")

  println("Operacje: +  -  *  /")
  println("Format: liczba operator liczba  (np. 10 + 5)")
  println("Wpisz 'exit' aby wyjsc")

  var running = true
  while (running) {
    val input = scala.io.StdIn.readLine("> ")
    input.trim match {
      case "exit" => running = false
      case line =>
        line.split(" ") match {
          case Array(a, "+", b) => kalkulator ! Add(a.toInt, b.toInt)
          case Array(a, "-", b) => kalkulator ! Subtract(a.toInt, b.toInt)
          case Array(a, "*", b) => kalkulator ! Multiply(a.toInt, b.toInt)
          case Array(a, "/", b) => kalkulator ! Divide(a.toInt, b.toInt)
          case _                => println("Nieprawidlowy format. Przyklad: 10 + 5")
        }
        Thread.sleep(100)
    }
  }

  system.terminate()
}