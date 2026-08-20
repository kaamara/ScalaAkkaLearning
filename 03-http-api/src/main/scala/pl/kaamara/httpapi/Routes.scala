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

/** Definicje tras HTTP.
  *
  * Podzial /health vs /ready jest celowy i odpowiada rozroznieniu
  * livenessProbe / readinessProbe w Kubernetesie:
  *   - /health: proces zyje. Porazka => restart poda.
  *   - /ready:  proces jest gotowy przyjmowac ruch. Porazka => pod
  *     wypada z Service, ale nie jest restartowany.
  * Mieszanie tych dwoch endpointow to najczestszy blad przy pierwszym
  * wdrozeniu na klaster: przeciazona aplikacja wpada w petle restartow.
  */
final class Routes(
    counter: ActorRef[CounterActor.Command],
    metrics: MetricsRegistry
)(implicit system: ActorSystem[_])
    extends JsonSupport {

  private implicit val timeout: Timeout    = Timeout(3.seconds)
  private implicit val ec: ExecutionContext = system.executionContext

  private val startedAt = System.currentTimeMillis()

  /** Przelacznik gotowosci — ustawiany na false przy rozpoczeciu
    * wygaszania, zeby load balancer przestal kierowac ruch zanim
    * proces zacznie sie zamykac.
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

  /** Owija trasy pomiarem czasu i zliczaniem odpowiedzi.
    * mapResponse widzi finalny status, wiec liczy takze 404 i 500.
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
      }(inner)
    }

  val routes: Route = instrumented(concat(healthRoutes, counterRoutes, metricsRoute))
}
