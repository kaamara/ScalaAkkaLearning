package pl.kaamara.httpapi

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.duration._

final class Routes(counter: ActorRef[CounterActor.Command])(implicit system: ActorSystem[_]) extends JsonSupport {

  private implicit val timeout: Timeout = 3.seconds

  private val metrics = new MetricsRegistry

  private val ready = new AtomicBoolean(true)
  def markNotReady(): Unit = ready.set(false)

  private def counterReply(command: ActorRef[CounterActor.Value] => CounterActor.Command): Route =
    onSuccess(counter.ask(command))(value => complete(CounterResponse(value.count)))

  private val api: Route = concat(
    path("health")(get(complete(StatusResponse("UP")))),
    path("ready")(get {
      if (ready.get()) complete(StatusResponse("READY"))
      else complete(StatusCodes.ServiceUnavailable -> StatusResponse("SHUTTING_DOWN"))
    }),
    path("counter")(get(counterReply(CounterActor.Get))),
    path("counter" / "increment")(post(counterReply(CounterActor.Increment))),
    path("counter" / "decrement")(post(counterReply(CounterActor.Decrement))),
    path("counter" / "reset")(post(counterReply(CounterActor.Reset))),
    path("metrics")(get {
      onSuccess(counter.ask(CounterActor.Get))(value => complete(metrics.render(value.count)))
    })
  )

  val routes: Route = extractRequest { request =>
    mapResponse { response =>
      metrics.record(request.method.value, request.uri.path.toString, response.status.intValue)
      response
    }(Route.seal(api))
  }
}
