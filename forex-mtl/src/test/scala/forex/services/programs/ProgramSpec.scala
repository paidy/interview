package forex.services.programs

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._
import forex.config.{ ProviderConfig, StorageConfig }
import forex.domain._
import forex.programs.rates.errors.Error.RateLookupFailed
import forex.programs.rates.{ Program, Protocol }
import forex.services.rates.interpreters.OneFrameResponse
import forex.services.{ RatesServices, StorageService, TestData }
import org.scalatest.{ EitherValues, OptionValues }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import sttp.client3.ResponseException
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend

import scala.concurrent.duration._

class ProgramSpec
    extends AsyncWordSpecLike
    with AsyncIOSpec
    with Matchers
    with OptionValues
    with EitherValues
    with TestData {
  "Program" should {
    "return error if rate is missing" in {
      val defaultConfig = StorageConfig(5.minutes, 1000)
      val backend = AsyncHttpClientFs2Backend
        .stub[IO]
        .whenRequestMatches(_.uri.paramsSeq.exists {
          case (key, _) => key == "pair"
        })
        .thenRespond(List.empty[Rate].asRight[ResponseException[_, _]])

      val cache   = StorageService.inMemory[IO](defaultConfig)
      val service = RatesServices.live(ProviderConfig("localhost:8080", "token"), backend)

      val program = Program[IO](service, cache)

      program.get(Protocol.GetRatesRequest(Currency.CAD, Currency.JPY)).map { result =>
        result shouldBe Symbol("Left")

        result.left.value shouldBe a[RateLookupFailed]
      }
    }

    "send request to get all rates once" in {
      val rates    = genRates.toList
      var reqCount = 0
      val backend = AsyncHttpClientFs2Backend
        .stub[IO]
        .whenRequestMatches(_.uri.paramsSeq.exists {
          case (key, _) => key == "pair"
        })
        .thenRespond {
          reqCount += 1
          rates
            .map(
              r =>
                OneFrameResponse(
                  r.pair.from,
                  r.pair.to,
                  r.price.value.doubleValue,
                  r.timestamp.value.toInstant.toString
              )
            )
            .asRight[ResponseException[_, _]]
        }

      val cache   = StorageService.inMemory[IO](StorageConfig(5.minutes, 1000))
      val service = RatesServices.live(ProviderConfig("localhost:8080", "token"), backend)

      val program = Program[IO](service, cache)

      for {
        req1 <- program.get(Protocol.GetRatesRequest(Currency.CAD, Currency.JPY))
        req2 <- program.get(Protocol.GetRatesRequest(Currency.CAD, Currency.JPY))
      } yield {
        val expected = rates.find(_.pair == Rate.Pair(Currency.CAD, Currency.JPY)).value

        req1.value shouldBe expected
        req2.value shouldBe expected
        reqCount shouldBe 1
      }
    }
  }
}
