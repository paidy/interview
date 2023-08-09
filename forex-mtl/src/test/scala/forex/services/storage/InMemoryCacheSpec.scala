package forex.services.storage

import cats.effect.IO
import cats.syntax.all._
import cats.effect.testing.scalatest.AsyncIOSpec
import forex.config.StorageConfig
import forex.domain.{ Currency, Price, Rate, Timestamp }
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

import scala.concurrent.duration._

class InMemoryCacheSpec extends AsyncWordSpecLike with AsyncIOSpec with Matchers with EitherValues {

  private val defaultConfig = StorageConfig(5.minutes)

  "InMemoryCache" should {
    "return an error on missing pair" in {
      val cache = Interpreters.inMemory[IO](defaultConfig)

      val pair = Rate.Pair(Currency.CAD, Currency.EUR)

      cache.get(pair).map { result =>
        result shouldBe Symbol("Left")

        result.left.value shouldBe a[errors.Error.PairLookupFailed]

        result.left.value.msg.contains(pair.show) shouldBe true
      }
    }

    "return a value for pair" in {
      val cache = Interpreters.inMemory[IO](defaultConfig)
      val rate  = Rate(Rate.Pair(Currency.JPY, Currency.CAD), Price(1), Timestamp.now)
      for {
        putResult <- cache.put(rate)
        getResult <- cache.get(Rate.Pair(Currency.JPY, Currency.CAD))
      } yield {
        putResult shouldBe Symbol("Right")
        getResult shouldBe Symbol("Right")
        getResult.value shouldBe rate
      }
    }

    "invalidate cache after a timeout" in {
      val cache = Interpreters.inMemory[IO](StorageConfig(50.millis))
      for {
        _ <- cache.put(Rate(Rate.Pair(Currency.JPY, Currency.CAD), Price(1), Timestamp.now))
        _ <- IO.sleep(100.millis)
        res <- cache.get(Rate.Pair(Currency.JPY, Currency.CAD))
      } yield res.left.value shouldBe a[errors.Error.PairLookupFailed]
    }
  }
}
