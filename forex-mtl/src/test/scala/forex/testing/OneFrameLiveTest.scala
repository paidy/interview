package forex.testing

import cats.Applicative
import forex.config.{ApplicationConfig, HttpConfig, UrlConfig}
//import cats.effect.IO
import cats.implicits._

import scala.concurrent.Future
//import cats.effect.testing.scalatest.AsyncIOSpec
import forex.domain._
//import forex.programs.rates.errors
import forex.services.rates.Interpreters
//import forex.services.rates.interpreters.OneFrameResponse
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import scala.concurrent.duration._

//import java.time.Instant

class OneFrameLiveTest
  extends AsyncWordSpecLike
    with Matchers
    with OptionValues
    with EitherValues {

  "OneFrameTestingLiveModule" should {
    "return an price on valid request" in {
      implicit val futureApplicative: Applicative[Future] = Applicative[Future]

      val httpConfig = HttpConfig("0.0.0.0", 8080, 40.seconds)
      val urlConfig = UrlConfig("http://localhost:8080", "10dc303535874aeccc86a8251e6992f5")
      val config = ApplicationConfig(httpConfig, urlConfig)
      val service = Interpreters.live(config)(futureApplicative)
      val result = service.get(Rate.Pair(Currency.GBP, Currency.USD))

      result.map { result =>
        result shouldBe Symbol("Right")
      }
    }
    "return an error on invalid currency request" in {
      implicit val futureApplicative: Applicative[Future] = Applicative[Future]
      val httpConfig = HttpConfig("0.0.0.0", 8080, 40.seconds)
      val urlConfig = UrlConfig("http://localhost:8080", "10dc303535874aeccc86a8251e6992f5")
      val config = ApplicationConfig(httpConfig, urlConfig)

      val service = Interpreters.live(config)(futureApplicative)
      val result = service.get(Rate.Pair(Currency.GBP, Currency.UNK))

      result.map { result =>
        result shouldBe Symbol("Left")
      }
    }
  }

}
