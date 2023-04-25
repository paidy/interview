package forex.http.external.oneframe

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import forex.domain.model.Currency.{EUR, JPY, USD}
import forex.domain.model.Rate
import org.http4s._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

class OneFrameHttpClientTest extends AsyncFunSpec with AsyncIOSpec with Matchers {
  // Define a mock HTTP service that emulates the behavior of the external API
  val mockService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> Root / "rates" =>
      val pairs = req.multiParams.getOrElse("pair", List.empty)
      if (pairs.nonEmpty) {
        Ok("""
            [{"from":"USD","to":"JPY","bid":0.61,"ask":0.82,"price":0.71,"time_stamp":"2019-01-01T00:00:00.000Z"},{"from":"EUR","to":"USD","bid":0.68,"ask":0.80,"price":0.68,"time_stamp":"2019-01-01T00:00:00.000Z"}]
          """)
      } else {
        BadRequest("No pair query parameter provided")
      }
  }

  val mockHttpApp: HttpApp[IO] = mockService.orNotFound
  val client                   = new OneFrameHttpClient[IO]("test-token", Client.fromHttpApp(mockHttpApp))

  describe("OneFrameHttpClient") {
    it("should fetch rates for given currency pairs") {
      val pairs = Seq(Rate.Pair(USD, JPY), Rate.Pair(EUR, USD))
      client.getRates(pairs).asserting { rates =>
        rates.length shouldEqual 2
        rates.head.pair.from shouldEqual USD
        rates.head.pair.to shouldEqual JPY

        rates.last.pair.from shouldEqual EUR
        rates.last.pair.to shouldEqual USD
      }
    }
  }
}
