package pl.kaamara.httpapi

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.pattern.after

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[CounterActor.Command] = ActorSystem(CounterActor(), "http-api")
    import system.executionContext

    val routes = new Routes(system)

    Http().newServerAt("0.0.0.0", 8080).bind(routes.routes).onComplete {
      case Success(binding) =>
        binding.addToCoordinatedShutdown(10.seconds)
        CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "markNotReady") { () =>
          routes.markNotReady()
          after(3.seconds)(Future.successful(Done))
        }
        system.log.info("Serwer dziala na porcie 8080")

      case Failure(e) =>
        system.log.error("Nie udalo sie uruchomic serwera", e)
        sys.exit(1)
    }
  }
}
