package forex.services.rates

import cats.effect.IO
import forex.domain._
import forex.services.rates.interpreters.OneFrameInterpreter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.http4s.client.Client
import org.http4s.{HttpApp, Response, Status, Uri}
import org.http4s.circe._
import io.circe.syntax._
import io.circe.generic.auto._

class RateServiceTest extends AnyFlatSpec with Matchers {
  "RateService" should "retrieve a valid rate for a currency pair" in {
    val expectedRateResponse = RateResponse(
      from = Currency.USD,
      to = Currency.JPY,
      bid = BigDecimal(103.5),
      ask = BigDecimal(104.0),
      price = Price(BigDecimal(103.75)),
      timeStamp = Timestamp.now
    )

    val stubClient: Client[IO] = Client.fromHttpApp[IO](
      HttpApp[IO] {
        case _ =>
          IO(Response[IO](status = Status.Ok).withEntity(expectedRateResponse.asJson))
      }
    )

    val oneFrameUri = Uri.uri("http://localhost/rates")
    val apiToken = "dummyToken"

    val service = new OneFrameInterpreter[IO](oneFrameUri, apiToken)
    val result = service.get(List(Rate.Pair(Currency.USD, Currency.JPY))).unsafeRunSync()

    result should matchPattern { case Right(_) => }
    result.foreach { rates =>
      rates.headOption.foreach { rateResponse =>
        rateResponse.from shouldBe Currency.USD
        rateResponse.to shouldBe Currency.JPY
        rateResponse.price.value should be > BigDecimal(0)
      }
    }
  }
}
