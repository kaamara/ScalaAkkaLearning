package pl.kaamara.httpapi

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

final case class CounterResponse(count: Long)
final case class StatusResponse(status: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val counterFormat: RootJsonFormat[CounterResponse] = jsonFormat1(CounterResponse)
  implicit val statusFormat: RootJsonFormat[StatusResponse]   = jsonFormat1(StatusResponse)
}
