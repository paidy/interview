package forex.services.integration

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import forex.config.StorageConfig
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.services.{ rates, RatesServices, StorageService }
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

import scala.concurrent.duration._

class OneFrameSpec extends AsyncWordSpecLike with AsyncIOSpec with Matchers with EitherValues {
  "OneFrameLive" should {
    "return a value for a pair from a repo" in {
      val defaultConfig = StorageConfig(5.minutes)
      val cache         = StorageService.inMemory[IO](defaultConfig)
      val service       = RatesServices.live(cache)

      for {
        _ <- cache.put(Rate(Rate.Pair(Currency.USD, Currency.JPY), Price(10), Timestamp.now))
        _ <- cache.put(Rate(Rate.Pair(Currency.CAD, Currency.EUR), Price(1), Timestamp.now))
        _ <- cache.put(Rate(Rate.Pair(Currency.USD, Currency.EUR), Price(1.1d), Timestamp.now))
        resUSDJPY <- service.get(Rate.Pair(Currency.USD, Currency.JPY))
        resUSDEUR <- service.get(Rate.Pair(Currency.USD, Currency.EUR))
        resCADEUR <- service.get(Rate.Pair(Currency.CAD, Currency.EUR))
        resCADJPY <- service.get(Rate.Pair(Currency.CAD, Currency.JPY))
      } yield {
        resUSDJPY.value.price shouldBe Price(10)
        resUSDEUR.value.price shouldBe Price(1.1d)
        resCADEUR.value.price shouldBe Price(1)
        resCADJPY.left.value shouldBe a[rates.errors.Error.OneFrameLookupFailed]
      }
    }
  }
}
