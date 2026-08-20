package pl.kaamara.httpapi

import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** ScalatestRouteTest tworzy klasyczny ActorSystem. Zamieniamy go na
  * typed przez `.toTyped` — to standardowy most miedzy oboma API.
  */
class RoutesSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with JsonSupport {

  private def newRoutes(): Routes = {
    implicit val typedSystem: akka.actor.typed.ActorSystem[Nothing] = system.toTyped
    val counter = system.spawn(CounterActor(0L), s"counter-${java.util.UUID.randomUUID()}")
    new Routes(counter, new MetricsRegistry())
  }

  "GET /health" should {
    "zwrocic 200 i status UP" in {
      Get("/health") ~> newRoutes().routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[HealthResponse].status shouldBe "UP"
      }
    }
  }

  "GET /ready" should {
    "zwrocic 200 gdy aplikacja przyjmuje ruch" in {
      Get("/ready") ~> newRoutes().routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[ReadyResponse].status shouldBe "READY"
      }
    }

    "zwrocic 503 po oznaczeniu jako not ready" in {
      val r = newRoutes()
      r.markNotReady()
      Get("/ready") ~> r.routes ~> check {
        status shouldBe StatusCodes.ServiceUnavailable
      }
    }
  }

  "GET /counter" should {
    "zwrocic wartosc poczatkowa 0" in {
      Get("/counter") ~> newRoutes().routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[CounterResponse].count shouldBe 0L
      }
    }
  }

  "POST /counter/increment" should {
    "zwiekszyc licznik i zwrocic nowa wartosc" in {
      val r = newRoutes()
      Post("/counter/increment") ~> r.routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[CounterResponse].count shouldBe 1L
      }
      Post("/counter/increment") ~> r.routes ~> check {
        responseAs[CounterResponse].count shouldBe 2L
      }
      Get("/counter") ~> r.routes ~> check {
        responseAs[CounterResponse].count shouldBe 2L
      }
    }

    "zwrocic 405 na metode GET" in {
      Get("/counter/increment") ~> newRoutes().routes ~> check {
        status shouldBe StatusCodes.MethodNotAllowed
      }
    }
  }

  "POST /counter/reset" should {
    "wyzerowac licznik" in {
      val r = newRoutes()
      Post("/counter/increment") ~> r.routes ~> check { status shouldBe StatusCodes.OK }
      Post("/counter/reset") ~> r.routes ~> check {
        responseAs[CounterResponse].count shouldBe 0L
      }
    }
  }

  "GET /metrics" should {
    "zwrocic tekst w formacie Prometheusa" in {
      val r = newRoutes()
      Get("/health") ~> r.routes ~> check { status shouldBe StatusCodes.OK }

      Get("/metrics") ~> r.routes ~> check {
        status shouldBe StatusCodes.OK
        val body = responseAs[String]
        body should include("# TYPE http_requests_total counter")
        body should include("""http_requests_total{method="GET",path="/health",status="200"}""")
        body should include("app_counter_value")
        body should include("jvm_memory_used_bytes")
      }
    }

    "policzyc 405 na zlej metodzie" in {
      val r = newRoutes()
      Get("/counter/increment") ~> r.routes ~> check {
        status shouldBe StatusCodes.MethodNotAllowed
      }
      Get("/metrics") ~> r.routes ~> check {
        responseAs[String] should include(
          """http_requests_total{method="GET",path="/counter/increment",status="405"} 1"""
        )
      }
    }

    "policzyc 404 i zwinac sciezke, zeby nie rozsadzic kardynalnosci" in {
      val r = newRoutes()
      Get("/nie-ma-takiego") ~> r.routes ~> check { status shouldBe StatusCodes.NotFound }
      Get("/tez-nie-ma")     ~> r.routes ~> check { status shouldBe StatusCodes.NotFound }
      Get("/metrics") ~> r.routes ~> check {
        val body = responseAs[String]
        body should include("""http_requests_total{method="GET",path="/{unmatched}",status="404"} 2""")
        body should not include "nie-ma-takiego"
        body should not include "tez-nie-ma"
      }
    }

    "odzwierciedlac aktualna wartosc licznika" in {
      val r = newRoutes()
      Post("/counter/increment") ~> r.routes ~> check { status shouldBe StatusCodes.OK }
      Get("/metrics") ~> r.routes ~> check {
        responseAs[String] should include("app_counter_value 1")
      }
    }
  }
}
