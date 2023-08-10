package forex.services.rates

import cats.effect.IO
import cats.implicits._
import cats.effect.testing.scalatest.AsyncIOSpec
import forex.config.ProviderConfig
import forex.domain._
import forex.services.TestData
import forex.services.rates.interpreters.OneFrameResponse
import org.scalatest.{ EitherValues, OptionValues }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import sttp.client3.{ HttpError, ResponseException }
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import sttp.model.StatusCode

import java.time.Instant

class OneFrameLiveSpec
    extends AsyncWordSpecLike
    with AsyncIOSpec
    with Matchers
    with OptionValues
    with EitherValues
    with TestData {

  "OneFrameLive" should {
    "return an error on wrong request" in {
      val backend = AsyncHttpClientFs2Backend
        .stub[IO]
        .whenAnyRequest
        .thenRespond(HttpError("Bad Request", StatusCode.BadRequest).asLeft[Rate])

      val service = Interpreters.live(ProviderConfig("localhost:8080", "token"), backend)

      service.get(Rate.Pair(Currency.GBP, Currency.USD)).map { result =>
        result shouldBe Symbol("Left")

        result.left.value shouldBe a[errors.Error.ApiRequestFailed]
      }
    }

    "return a value for a pair" in {
      val pair = Rate.Pair(Currency.GBP, Currency.USD)
      val backend = AsyncHttpClientFs2Backend
        .stub[IO]
        .whenRequestMatches(_.uri.paramsSeq.exists {
          case (key, value) => key == "pair" && value == pair.show
        })
        .thenRespond(
          List(OneFrameResponse(pair.from, pair.to, 10d, Instant.now().toString)).asRight[ResponseException[_, _]]
        )

      val service = Interpreters.live(ProviderConfig("localhost:8080", "token"), backend)

      service.get(pair).map { result =>
        result shouldBe Symbol("Right")

        result.value.price shouldBe Price(10)
      }
    }

    "return values for all pairs" in {
      val rates = genRates

      val backend = AsyncHttpClientFs2Backend
        .stub[IO]
        .whenRequestMatches(_.uri.paramsSeq.exists {
          case (key, _) => key == "pair"
        })
        .thenRespond(
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
            .toList
            .asRight[ResponseException[_, _]]
        )

      val service = Interpreters.live(ProviderConfig("localhost:8080", "token"), backend)

      service.getAll.map { result =>
        result shouldBe Symbol("Right")

        result.value should contain theSameElementsAs rates
      }
    }
  }

}
