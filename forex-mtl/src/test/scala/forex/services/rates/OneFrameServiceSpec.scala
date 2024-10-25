package forex.services.rates

import cats.effect.{Async, IO}
import forex.cache.CurrencyRateCacheAlgebra
import forex.cache.errors.Error
import forex.config.{HttpOneFrameConfig, OneFrameConfig, Token}
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.errors.Error.OneFrameLookupFailed
import forex.services.rates.interpreters.OneFrameService
import forex.services.rates.token.{TokenCacheAlgebra, TokenProvider}
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatest.wordspec.AsyncWordSpecLike
import sttp.client3.testing._

import java.time.OffsetDateTime
class OneFrameServiceSpec extends AsyncWordSpecLike with Matchers {

  val response: String =
    """
      |[
      |  {
      |    "from": "USD",
      |    "to": "EUR",
      |    "bid": 0.8702743979669029,
      |    "ask": 0.8129834411047454,
      |    "price": 0.84162891953582,
      |    "time_stamp": "2020-07-04T17:56:12.907Z"
      |  }
      |]
      |""".stripMargin

  class LocalCurrencyRateCacheAlgebra[F[_]: Async] extends CurrencyRateCacheAlgebra[F] {
    private val localCache = scala.collection.mutable.Map[String, Rate]()

    override def getRates(key: String): F[Error Either Rate] =
      localCache.get(key) match {
        case Some(rate) => Async[F].pure(Right(rate))
        case None       => Async[F].pure(Left(Error.KeyNotFoundInCache(s"key not found in cache $key")))
      }

    override def updateRates(key: String, rate: Rate, timeoutInSeconds: Int): F[String] = {
      localCache.put(key, rate)
      Async[F].pure("")
    }
  }

  class LocalTokenCache[F[_]: Async] extends TokenCacheAlgebra[F] {
    private val localCache = scala.collection.mutable.Map[String, Int]()

    def addToken(token: String): Option[Int] =
      localCache.put(token, 0)

    override def getToken(): F[Option[String]] = Async[F].pure(localCache.headOption.map(_._1))

    override def incrementUsage(token: String): F[String] =
      localCache
        .get(token)
        .map { value =>
          localCache.put(token, value + 1).map { _ =>
            Async[F].pure("")
          }
        }
        .get
        .get
  }

  "OneFrameService" should {
    "return a rate from the OneFrame service when not available in cache also update the cache" in {
      val token  = Token(List("123", "456"), 5.seconds, 10)
      val config = OneFrameConfig(HttpOneFrameConfig("host", 8080, 5.seconds, token), 5.seconds)
      val pair   = Rate.Pair(Currency.USD, Currency.EUR)
      val rate =
        Rate(pair, Price(BigDecimal(0.84162891953582)), Timestamp(OffsetDateTime.parse("2020-07-04T17:56:12.907Z")))

      // Stubbing the HTTP backend
      val backend = SttpBackendStub.synchronous
        .whenRequestMatches(
          _.uri.toString == s"http://${config.http.host}:${config.http.port}/rates?pair=${pair.from}${pair.to}"
        )
        .thenRespond(response)
      val currencyCache      = new LocalCurrencyRateCacheAlgebra[IO]()
      val oneFrameTokenCache = new LocalTokenCache[IO]()
      oneFrameTokenCache.addToken("123")
      oneFrameTokenCache.addToken("456")
      val tokenProvider = new TokenProvider[IO](IO(oneFrameTokenCache))
      val service       = OneFrameService[IO](config, backend, currencyCache, tokenProvider)

      currencyCache.getRates("rates:USD-EUR") map { result =>
        result shouldBe Right(rate)
      }
      service.get(pair).unsafeToFuture() map { result =>
        result shouldBe Right(rate)
      }
    }

    "return a rate from the cache if it's available" in {
      // ... existing setup code ...
      val token   = Token(List("123", "456"), 5.seconds, 10)
      val pair    = Rate.Pair(Currency.USD, Currency.EUR)
      val backend = SttpBackendStub.synchronous
      val rate =
        Rate(pair, Price(BigDecimal(0.84162891953582)), Timestamp(OffsetDateTime.parse("2020-07-04T17:56:12.907Z")))
      val config = OneFrameConfig(HttpOneFrameConfig("host", 8080, 5.seconds, token), 5.seconds)
      // Pre-populate cache with a rate
      val cacheKey      = "rates:USD-EUR"
      val currencyCache = new LocalCurrencyRateCacheAlgebra[IO]()
      currencyCache.updateRates(cacheKey, rate, 300)
      val oneFrameTokenCache = new LocalTokenCache[IO]()
      val tokenProvider      = new TokenProvider[IO](IO(oneFrameTokenCache))
      val service            = OneFrameService[IO](config, backend, currencyCache, tokenProvider)
      // Call the service
      service.get(pair).unsafeToFuture() map { result =>
        result shouldBe Right(rate)
      }
    }
  }

  "handle an error from the OneFrame service" in {
    // ... existing setup code ...
    val token   = Token(List("123", "456"), 5.seconds, 10)
    val pair    = Rate.Pair(Currency.USD, Currency.EUR)
    val config  = OneFrameConfig(HttpOneFrameConfig("host", 8080, 5.seconds, token), 5.seconds)
    val backend = SttpBackendStub.synchronous
    // Simulate an error response from the OneFrame service
    backend
      .whenRequestMatches(_.uri.toString.contains("/rates"))
      .thenRespondServerError()
    val oneFrameTokenCache = new LocalTokenCache[IO]()
    val tokenProvider      = new TokenProvider[IO](IO(oneFrameTokenCache))
    val currencyCache      = new LocalCurrencyRateCacheAlgebra[IO]()
    val service            = OneFrameService[IO](config, backend, currencyCache, tokenProvider)
    service.get(pair).unsafeToFuture() map { result =>
      result shouldBe Left(OneFrameLookupFailed("Internal Service Error"))
    }
  }

  "handle a situation where no valid tokens are available" in {
    // ... existing setup code ...
    val token   = Token(List("123", "456"), 5.seconds, 10)
    val pair    = Rate.Pair(Currency.USD, Currency.EUR)
    val config  = OneFrameConfig(HttpOneFrameConfig("host", 8080, 5.seconds, token), 5.seconds)
    val backend = SttpBackendStub.synchronous
    // Clear all tokens from the token cache
    val oneFrameTokenCache = new LocalTokenCache[IO]()
    val tokenProvider      = new TokenProvider[IO](IO(oneFrameTokenCache))
    val currencyCache      = new LocalCurrencyRateCacheAlgebra[IO]()
    val service            = OneFrameService[IO](config, backend, currencyCache, tokenProvider)
    service.get(pair).unsafeToFuture() map { result =>
      result shouldBe Left(OneFrameLookupFailed("Internal Service Error"))
    }
  }

  "handle a timeout or failure in the HTTP request" in {
    // ... existing setup code ...
    val pair    = Rate.Pair(Currency.USD, Currency.EUR)
    val token   = Token(List("123", "456"), 5.seconds, 10)
    val backend = SttpBackendStub.synchronous
    val config  = OneFrameConfig(HttpOneFrameConfig("host", 8080, 5.seconds, token), 5.seconds)
    // Simulate a request timeout
    backend
      .whenRequestMatches(_.uri.toString.contains("/rates"))
      .thenRespond(IO.raiseError(new RuntimeException("Request timeout")))
    val oneFrameTokenCache = new LocalTokenCache[IO]() // You might need to implement this method
    oneFrameTokenCache.addToken("123")
    val tokenProvider = new TokenProvider[IO](IO(oneFrameTokenCache))
    val currencyCache = new LocalCurrencyRateCacheAlgebra[IO]()

    val service = OneFrameService[IO](config, backend, currencyCache, tokenProvider)
    service.get(pair).unsafeToFuture() map { result =>
      result shouldBe Left(OneFrameLookupFailed("Internal Service Error"))
    }
  }

}
