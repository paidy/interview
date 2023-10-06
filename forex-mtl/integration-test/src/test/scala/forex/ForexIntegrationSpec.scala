package forex

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.typesafe.scalalogging.LazyLogging
import forex.clients.rates.OneFrameClient
import forex.model.config.ApplicationConfig
import forex.model.domain.{Currency, Price, Rate, Timestamp}
import io.circe.generic.extras.Configuration
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import pureconfig.generic.auto._


class ForexIntegrationSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers with LazyLogging{

  val appConfig: ApplicationConfig = ConfigSource
    .default.at("app").loadOrThrow[ApplicationConfig]

  val fakeRatePair: Rate.Pair = Rate.Pair(Currency.CAD, Currency.CHF)

  it should "One-Frame service should be up" in {
    val ratesCache = new OneFrameClient[IO](appConfig.oneFrameClient)

    ratesCache
      .get(Set(fakeRatePair), appConfig.oneFrameService.oneFrameTokens.head)
      .recoverWith { err =>
        logger.error("One-Frame service error", err)
        IO.raiseError(err)
      }
      .map { res =>
        logger.info("One-Frame service response: " + res)
        res should not be empty
      }
  }




}
