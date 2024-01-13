package forex.http.rates

import cats.Applicative
import cats.effect.{IO, Resource}
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId}
import forex.config.RateLimitConfig
import forex.domain.{Price, Rate, Timestamp}
import forex.http.ratelimitter.RedisRateLimiter
import forex.programs.RatesProgram
import forex.services.RatesService
import forex.services.rates.Algebra
import forex.services.rates.errors.Error
import org.http4s._
import org.http4s.implicits._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import org.typelevel.ci.CIString
import redis.clients.jedis.Jedis
import redis.clients.jedis.params.SetParams

import scala.concurrent.duration.DurationInt

class RatesHttpRoutesSpec extends AsyncWordSpecLike with Matchers {

  class OneFrameMock[F[_]: Applicative] extends Algebra[F] {

    override def get(pair: Rate.Pair): F[Error Either Rate] =
      Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[Error].pure[F]

  }

  class MockJedis(getResponse: String, setResponse: String = "OK") extends Jedis {
    override def get(key: String): String = getResponse

    override def set(key: String, value: String, params: SetParams): String = setResponse

    override def incr(key: String): java.lang.Long = 1L
  }

  class MockExceptionJedis extends Jedis {
    override def get(key: String): String = throw new Exception("Something went wrong")

    override def set(key: String, value: String, params: SetParams): String = "OK"

    override def incr(key: String): java.lang.Long = 1L
  }

  "RatesHttpRoutes" should {
    "respond with BadRequest when token header is missing" in {
      // Mock the dependencies
      val config                         = RateLimitConfig(1, 5.seconds)
      val tokens                         = List("123", "456")
      val ratesService: RatesService[IO] = new OneFrameMock[IO]

      val ratesProgram: RatesProgram[IO] = RatesProgram[IO](ratesService)
      val mockJedisResource: Resource[IO, Jedis] = Resource.make(
        IO.pure(new MockJedis(null))
      ) { jedis =>
        IO.pure(jedis.close()).handleErrorWith(_ => IO.unit)
      }

      val rateLimiter = RedisRateLimiter[IO](mockJedisResource, config, tokens)

      // Create an instance of RatesHttpRoutes
      val routes = new RatesHttpRoutes[IO](ratesProgram, rateLimiter).routes.orNotFound

      // Create a request
      val request = Request[IO](Method.GET, uri"/rates?from=USD&to=EUR")

      // Make the request and check the response
      routes
        .run(request)
        .flatMap { response =>
          response.status shouldBe Status.BadRequest

          // Decode the response body as String
          response.as[String].map { responseBody =>
            responseBody shouldBe "Token header is missing"
          }
        }
        .unsafeToFuture()
    }

    "respond with Forbidden when token header contains invalid token" in {
      // Mock the dependencies
      val config                         = RateLimitConfig(1, 5.seconds)
      val tokens                         = List("123", "456")
      val ratesService: RatesService[IO] = new OneFrameMock[IO]

      val ratesProgram: RatesProgram[IO] = RatesProgram[IO](ratesService)
      val mockJedisResource: Resource[IO, Jedis] = Resource.make(
        IO.pure(new MockJedis(null))
      ) { jedis =>
        IO.pure(jedis.close()).handleErrorWith(_ => IO.unit)
      }

      val rateLimiter = RedisRateLimiter[IO](mockJedisResource, config, tokens)

      // Create an instance of RatesHttpRoutes
      val routes = new RatesHttpRoutes[IO](ratesProgram, rateLimiter).routes.orNotFound

      // Create a request
      val request =
        Request[IO](Method.GET, uri"/rates?from=USD&to=EUR", headers = Headers(Header.Raw(CIString("token"), "789")))
      // Make the request and check the response
      routes
        .run(request)
        .flatMap { response =>
          response.status shouldBe Status.Forbidden

          // Decode the response body as String
          response.as[String].map { responseBody =>
            responseBody shouldBe "Invalid Token"
          }
        }
        .unsafeToFuture()
    }

    "respond with TooManyRequests when token is exhausted" in {
      // Mock the dependencies
      val config                         = RateLimitConfig(1, 5.seconds)
      val tokens                         = List("123", "456")
      val ratesService: RatesService[IO] = new OneFrameMock[IO]

      val ratesProgram: RatesProgram[IO] = RatesProgram[IO](ratesService)
      val mockJedisResource: Resource[IO, Jedis] = Resource.make(
        IO.pure(new MockJedis("2"))
      ) { jedis =>
        IO.pure(jedis.close()).handleErrorWith(_ => IO.unit)
      }

      val rateLimiter = RedisRateLimiter[IO](mockJedisResource, config, tokens)

      // Create an instance of RatesHttpRoutes
      val routes = new RatesHttpRoutes[IO](ratesProgram, rateLimiter).routes.orNotFound

      // Create a request
      val request =
        Request[IO](Method.GET, uri"/rates?from=USD&to=EUR", headers = Headers(Header.Raw(CIString("token"), "123")))
      // Make the request and check the response
      routes
        .run(request)
        .flatMap { response =>
          response.status shouldBe Status.TooManyRequests

          // Decode the response body as String
          response.as[String].map { responseBody =>
            responseBody shouldBe "Token 123 exhausted the limit 1"
          }
        }
        .unsafeToFuture()
    }

    "respond with BadRequest when from Currency is not supported" in {
      // Mock the dependencies
      val config                         = RateLimitConfig(1, 5.seconds)
      val tokens                         = List("123", "456")
      val ratesService: RatesService[IO] = new OneFrameMock[IO]

      val ratesProgram: RatesProgram[IO] = RatesProgram[IO](ratesService)
      val mockJedisResource: Resource[IO, Jedis] = Resource.make(
        IO.pure(new MockJedis("0"))
      ) { jedis =>
        IO.pure(jedis.close()).handleErrorWith(_ => IO.unit)
      }

      val rateLimiter = RedisRateLimiter[IO](mockJedisResource, config, tokens)

      // Create an instance of RatesHttpRoutes
      val routes = new RatesHttpRoutes[IO](ratesProgram, rateLimiter).routes.orNotFound

      // Create a request
      val request =
        Request[IO](Method.GET, uri"/rates?from=XXX&to=EUR", headers = Headers(Header.Raw(CIString("token"), "123")))

      // Make the request and check the response
      routes
        .run(request)
        .flatMap { response =>
          response.status shouldBe Status.BadRequest

          // Decode the response body as String
          response.as[String].map { responseBody =>
            responseBody shouldBe "Invalid currency from"
          }
        }
        .unsafeToFuture()
    }

    "respond with BadRequest when To Currency is not supported" in {
      // Mock the dependencies
      val config                         = RateLimitConfig(1, 5.seconds)
      val tokens                         = List("123", "456")
      val ratesService: RatesService[IO] = new OneFrameMock[IO]

      val ratesProgram: RatesProgram[IO] = RatesProgram[IO](ratesService)
      val mockJedisResource: Resource[IO, Jedis] = Resource.make(
        IO.pure(new MockJedis("0"))
      ) { jedis =>
        IO.pure(jedis.close()).handleErrorWith(_ => IO.unit)
      }

      val rateLimiter = RedisRateLimiter[IO](mockJedisResource, config, tokens)

      // Create an instance of RatesHttpRoutes
      val routes = new RatesHttpRoutes[IO](ratesProgram, rateLimiter).routes.orNotFound

      // Create a request
      val request =
        Request[IO](Method.GET, uri"/rates?from=USD&to=XXX", headers = Headers(Header.Raw(CIString("token"), "123")))

      // Make the request and check the response
      routes
        .run(request)
        .flatMap { response =>
          response.status shouldBe Status.BadRequest

          // Decode the response body as String
          response.as[String].map { responseBody =>
            responseBody shouldBe "Invalid currency to"
          }
        }
        .unsafeToFuture()
    }

    "respond with NotFound when Currency is not present" in {
      // Mock the dependencies
      val config                         = RateLimitConfig(1, 5.seconds)
      val tokens                         = List("123", "456")
      val ratesService: RatesService[IO] = new OneFrameMock[IO]

      val ratesProgram: RatesProgram[IO] = RatesProgram[IO](ratesService)
      val mockJedisResource: Resource[IO, Jedis] = Resource.make(
        IO.pure(new MockJedis("0"))
      ) { jedis =>
        IO.pure(jedis.close()).handleErrorWith(_ => IO.unit)
      }

      val rateLimiter = RedisRateLimiter[IO](mockJedisResource, config, tokens)

      // Create an instance of RatesHttpRoutes
      val routes = new RatesHttpRoutes[IO](ratesProgram, rateLimiter).routes.orNotFound

      // Create a request
      val request =
        Request[IO](Method.GET, uri"/rates?from=USD", headers = Headers(Header.Raw(CIString("token"), "123")))

      // Make the request and check the response
      routes
        .run(request)
        .flatMap { response =>
          response.status shouldBe Status.NotFound

          // Decode the response body as String
          response.as[String].map { responseBody =>
            responseBody shouldBe "Not found"
          }
        }
        .unsafeToFuture()
    }

  }

  "respond with Proper InternalServerError when RateLimitter is throwing exception" in {
    // Mock the dependencies
    val config                         = RateLimitConfig(1, 5.seconds)
    val tokens                         = List("123", "456")
    val ratesService: RatesService[IO] = new OneFrameMock[IO]

    val ratesProgram: RatesProgram[IO] = RatesProgram[IO](ratesService)
    val mockJedisResource: Resource[IO, Jedis] = Resource.make(
      IO.pure(new MockExceptionJedis())
    ) { jedis =>
      IO.pure(jedis.close()).handleErrorWith(_ => IO.unit)
    }

    val rateLimiter = RedisRateLimiter[IO](mockJedisResource, config, tokens)

    // Create an instance of RatesHttpRoutes
    val routes = new RatesHttpRoutes[IO](ratesProgram, rateLimiter).routes.orNotFound

    // Create a request
    val request =
      Request[IO](Method.GET, uri"/rates?from=USD&to=XXX", headers = Headers(Header.Raw(CIString("token"), "123")))

    // Make the request and check the response
    routes
      .run(request)
      .flatMap { response =>
        response.status shouldBe Status.InternalServerError

        // Decode the response body as String
        response.as[String].map { responseBody =>
          responseBody shouldBe "Something went wrong"
        }
      }
      .unsafeToFuture()
  }
}
