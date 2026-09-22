package pl.kaamara.httpapi

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/** Wszystkie adresy HTTP aplikacji.
  *
  * Sa dwa osobne adresy do sprawdzania stanu i to nie pomylka:
  *   - /health mowi "aplikacja zyje"       -> gdy nie odpowiada, restart
  *   - /ready  mowi "moge przyjmowac ruch" -> gdy nie odpowiada, tylko
  *     przestaje dostawac zadania, bez restartu
  */
final class Routes(
    counter: ActorRef[CounterActor.Command],
    metrics: MetricsRegistry
)(implicit system: ActorSystem[_])
    extends JsonSupport {

  private implicit val timeout: Timeout    = Timeout(3.seconds)
  private implicit val ec: ExecutionContext = system.executionContext

  private val startedAt = System.currentTimeMillis()

  /** Przelacznik "przyjmuje ruch". Przy zamykaniu idzie na false, zeby
    * nowe zadania przestaly naplywac, zanim aplikacja sie wylaczy.
    */
  private val ready = new AtomicBoolean(true)
  def markNotReady(): Unit = ready.set(false)

  private def uptimeSeconds: Long = (System.currentTimeMillis() - startedAt) / 1000

  private val healthRoutes: Route =
    concat(
      path("health") {
        get {
          complete(HealthResponse("UP", uptimeSeconds))
        }
      },
      path("ready") {
        get {
          if (ready.get()) {
            complete(ReadyResponse("READY", Map("actorSystem" -> "UP")))
          } else {
            complete(StatusCodes.ServiceUnavailable -> ReadyResponse("SHUTTING_DOWN", Map.empty))
          }
        }
      }
    )

  private val counterRoutes: Route =
    pathPrefix("counter") {
      concat(
        pathEnd {
          get {
            onComplete(counter.ask(CounterActor.Get)) {
              case Success(v) => complete(CounterResponse(v.count))
              case Failure(e) => complete(StatusCodes.ServiceUnavailable -> ErrorResponse(e.getMessage))
            }
          }
        },
        path("increment") {
          post {
            onComplete(counter.ask(CounterActor.Increment)) {
              case Success(v) => complete(CounterResponse(v.count))
              case Failure(e) => complete(StatusCodes.ServiceUnavailable -> ErrorResponse(e.getMessage))
            }
          }
        },
        path("decrement") {
          post {
            onComplete(counter.ask(CounterActor.Decrement)) {
              case Success(v) => complete(CounterResponse(v.count))
              case Failure(e) => complete(StatusCodes.ServiceUnavailable -> ErrorResponse(e.getMessage))
            }
          }
        },
        path("reset") {
          post {
            onComplete(counter.ask(CounterActor.Reset)) {
              case Success(v) => complete(CounterResponse(v.count))
              case Failure(e) => complete(StatusCodes.ServiceUnavailable -> ErrorResponse(e.getMessage))
            }
          }
        }
      )
    }

  private val metricsRoute: Route =
    path("metrics") {
      get {
        val body: Future[String] =
          counter.ask(CounterActor.Get).map(v => metrics.render(v.count))
        onComplete(body) {
          case Success(text) =>
            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, text))
          case Failure(e) =>
            complete(StatusCodes.ServiceUnavailable -> ErrorResponse(e.getMessage))
        }
      }
    }

  /** Mierzy czas kazdego zadania i zlicza odpowiedzi do metryk.
    *
    * Route.seal jest tu potrzebne. Bez niego zle adresy (404) w ogole nie
    * trafialyby do statystyk, a to pierwsza rzecz, ktorej sie szuka, gdy
    * klient puka pod zly adres.
    */
  private def instrumented(inner: Route): Route =
    extractRequest { req =>
      val start = System.nanoTime()
      mapResponse { resp =>
        metrics.record(
          req.method.value,
          req.uri.path.toString,
          resp.status.intValue,
          System.nanoTime() - start
        )
        resp
      }(Route.seal(inner))
    }

  val routes: Route = instrumented(concat(healthRoutes, counterRoutes, metricsRoute))
}
