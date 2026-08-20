package pl.kaamara.httpapi

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

final case class CounterResponse(count: Long)
final case class HealthResponse(status: String, uptimeSeconds: Long)
final case class ReadyResponse(status: String, checks: Map[String, String])
final case class ErrorResponse(error: String)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val counterFormat: spray.json.RootJsonFormat[CounterResponse] = jsonFormat1(CounterResponse)
  implicit val healthFormat: spray.json.RootJsonFormat[HealthResponse]   = jsonFormat2(HealthResponse)
  implicit val readyFormat: spray.json.RootJsonFormat[ReadyResponse]     = jsonFormat2(ReadyResponse)
  implicit val errorFormat: spray.json.RootJsonFormat[ErrorResponse]     = jsonFormat1(ErrorResponse)
}
