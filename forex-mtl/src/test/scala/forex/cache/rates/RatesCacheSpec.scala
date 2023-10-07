package forex.cache.rates

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import forex.model.config.CacheConfig
import forex.model.domain.{Currency, Price, Rate, Timestamp}

import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import scala.concurrent.duration._


class RatesCacheSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers {

  val config: CacheConfig = CacheConfig(
    expireTimeout = 2.seconds
  )

  val fakeRate: Rate = Rate(
    Rate.Pair(Currency.CAD, Currency.CHF), Price(123), Timestamp.now())

  it should "store new rates and return them" in {
    val ratesCache = new RatesCache[IO](config)

    ratesCache
      .update(List(fakeRate))
      .flatMap(_ => ratesCache.get(fakeRate.pair).value)
      .map { rate =>
        rate should not be empty
        rate.get shouldEqual fakeRate
      }
  }

  it should "remove stored rates after TTL expire" in {
    val ratesCache = new RatesCache[IO](config)

    ratesCache
      .update(List(fakeRate))
      .flatMap(_ => IO.sleep(2.seconds))
      .flatMap(_ => ratesCache.get(fakeRate.pair).value)
      .map { rate =>
        rate shouldBe empty
      }
  }
}
