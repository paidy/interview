package forex.services.storage

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import forex.config.StorageConfig
import forex.domain.{ Currency, Price, Rate, Timestamp }
import org.scalatest.{ EitherValues, OptionValues }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

import scala.concurrent.duration._

class InMemoryCacheSpec extends AsyncWordSpecLike with AsyncIOSpec with Matchers with EitherValues with OptionValues {

  private val defaultConfig = StorageConfig(5.minutes, 1000)

  "InMemoryCache" should {
    "return an error on missing pair" in {
      val cache = Interpreters.inMemory[IO](defaultConfig)

      val pair = Rate.Pair(Currency.CAD, Currency.EUR)

      cache.get(pair).map { result =>
        result shouldBe None
      }
    }

    "return a value for pair" in {
      val cache = Interpreters.inMemory[IO](defaultConfig)
      val rate  = Rate(Rate.Pair(Currency.JPY, Currency.CAD), Price(1), Timestamp.now)
      for {
        putResult <- cache.putAll(List(rate))
        getResult <- cache.get(Rate.Pair(Currency.JPY, Currency.CAD))
      } yield {
        putResult shouldBe ()
        getResult.value shouldBe rate
      }
    }

    "invalidate cache after a timeout" in {
      val cache = Interpreters.inMemory[IO](StorageConfig(50.millis, 10000000))
      for {
        _ <- cache.putAll(List(Rate(Rate.Pair(Currency.JPY, Currency.CAD), Price(1), Timestamp.now)))
        _ <- IO.sleep(100.millis)
        res <- cache.get(Rate.Pair(Currency.JPY, Currency.CAD))
      } yield res shouldBe None
    }
  }
}
