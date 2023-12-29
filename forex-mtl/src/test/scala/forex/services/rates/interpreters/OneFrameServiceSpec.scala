package forex.services.rates.interpreters

import cats.Applicative
import cats.effect._
import cats.implicits._
import forex.client.OneFrameClient
import forex.config.CacheConfig
import forex.domain._
import forex.services.rates.errors.Error
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scalacache.caffeine.CaffeineCache
import scalacache.modes.sync.mode

import java.time.OffsetDateTime
import scala.concurrent.duration.DurationInt

class OneFrameServiceSpec extends AnyFunSuite with Matchers {

  implicit val contextShiftInstance: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)
  implicit val timerInstance: Timer[IO]               = IO.timer(scala.concurrent.ExecutionContext.global)

  // Test case
  test("get should return cached rate if available") {
    // Mock OneFrameClient for testing
    class MockOneFrameClient[F[_]: Applicative] extends OneFrameClient[F] {
      override def getRates(
          pairs: Vector[Rate.Pair]
      ): F[Either[Error, List[OneFrameCurrencyInformation]]] = {
        val mockResponse = List(
          OneFrameCurrencyInformation(
            from = "USD",
            to = "JPY",
            bid = 0.4332159351433372,
            ask = 0.2056211718073755,
            price = 0.31941855347535635,
            time_stamp = "2023-12-29T17:22:46.691Z"
          )
        )

        Applicative[F].pure(mockResponse.asRight)
      }
    }
    val oneFrameClient = new MockOneFrameClient[IO]
    val cache          = CaffeineCache[Rate]
    val cacheConfig    = CacheConfig(5)
    val service        = new OneFrameService[IO](oneFrameClient, cache, cacheConfig)

    val pair = Rate.Pair(Currency.USD, Currency.EUR)
    val rate = Rate(pair, Price(BigDecimal(1.2)), Timestamp(OffsetDateTime.now()))

    // Put rate into the cache
    cache.put(pair.key)(rate, cacheConfig.oneFrameExpiry.minutes.some)

    val result = service.get(pair).unsafeRunSync()

    result shouldBe Right(rate)
  }

  // Negative test case
  test("get should return OneFrameLookupFailed for an exception during the call") {
    // Mock OneFrameClient for testing
    class MockOneFrameClientNegative[F[_]: Applicative] extends OneFrameClient[F] {
      override def getRates(
          pairs: Vector[Rate.Pair]
      ): F[Either[Error, List[OneFrameCurrencyInformation]]] = {
        val leftResponse = Error.OneFrameLookupFailed("Something went wrong...")
        Applicative[F].pure(leftResponse.asLeft)
      }
    }

    val oneFrameClientNegative = new MockOneFrameClientNegative[IO]
    val cacheNegative          = CaffeineCache[Rate]
    val cacheConfigNegative    = CacheConfig(5)
    val service                = new OneFrameService[IO](oneFrameClientNegative, cacheNegative, cacheConfigNegative)

    val pairNegative = Rate.Pair(Currency.USD, Currency.EUR)

    val resultNegative = service.get(pairNegative).unsafeRunSync()

    resultNegative shouldBe a[Left[_, _]]
    resultNegative.left.map(_.toString) shouldBe Left("OneFrameLookupFailed(Cache not updated. Please contact admin.)")
  }
}
