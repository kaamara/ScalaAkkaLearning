package pl.kaamara.httpapi

import akka.actor.CoordinatedShutdown
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Main {

  private val log = LoggerFactory.getLogger(getClass)

  /** Ustawienia czytane ze zmiennych srodowiskowych, nie z pliku.
    * Dzieki temu port zmienia sie bez budowania obrazu od nowa.
    */
  private def env(name: String, default: String): String =
    sys.env.getOrElse(name, default)

  def main(args: Array[String]): Unit = {
    val host = env("HTTP_HOST", "0.0.0.0")
    val port = env("HTTP_PORT", "8080").toInt

    val rootBehavior: Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
      implicit val system: ActorSystem[Nothing] = context.system
      import system.executionContext

      val counter: ActorRef[CounterActor.Command] =
        context.spawn(CounterActor(0L), "counter")

      val routes = new Routes(counter, new MetricsRegistry())

      Http()
        .newServerAt(host, port)
        .bind(routes.routes)
        .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))
        .onComplete {
          case Success(binding) =>
            val addr = binding.localAddress
            log.info("Nasluchiwanie na http://{}:{}", addr.getHostString, addr.getPort)

            // Przy zamykaniu najpierw mowimy "nie przyjmuje ruchu",
            // a dopiero potem zamykamy polaczenia.
            CoordinatedShutdown(system)
              .addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "markNotReady") { () =>
                log.info("Otrzymano sygnal zamkniecia — oznaczam jako not ready")
                routes.markNotReady()
                akka.pattern.after(3.seconds)(
                  scala.concurrent.Future.successful(akka.Done)
                )(system.classicSystem)
              }

          case Failure(ex) =>
            log.error("Nie udalo sie zbindowac serwera HTTP", ex)
            system.terminate()
        }

      Behaviors.empty
    }

    ActorSystem[Nothing](rootBehavior, "http-api")
  }
}
