package forex
import cats.effect._
import cats.implicits._
import forex.cache.RedisCurrencyRateCache
import forex.config.ApplicationConfig
import forex.http.ratelimitter.RedisRateLimiter
import forex.http.ratelimitter.interpreters.RateLimitterAlgebra
import forex.http.rates.RatesHttpRoutes
import forex.programs._
import forex.services._
import forex.services.rates.token.{RedisTokenCache, TokenCacheAlgebra, TokenProvider}
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import sttp.client3.{HttpURLConnectionBackend, Identity, SttpBackend}

class Module[F[_]: Concurrent: Timer](config: ApplicationConfig) {
  private val logger                      = LoggerFactory.getLogger(getClass)
  val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()
  private val jedisResource: Resource[F, Jedis] = Resource.make(
    Async[F].delay(new Jedis(config.redis.host, config.redis.port)) // Acquire the resource
  ) { jedis =>
    Async[F].delay(jedis.close()).handleErrorWith { e =>
      logger.error("Failed to close jedis resource", e)
      Async[F].unit
    }
  }
  private val currencyCache: RedisCurrencyRateCache[F] = new RedisCurrencyRateCache[F](jedisResource)
  private val oneFrameTokensCache: F[TokenCacheAlgebra[F]] =
    RedisTokenCache[F](
      jedisResource,
      config.oneFrame.http.token)
  private val tokenProvider: TokenProvider[F] = new TokenProvider[F](oneFrameTokensCache)
  private val ratesService: RatesService[F]   = RatesServices.live(config.oneFrame, backend, currencyCache, tokenProvider)

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService)

  val ratesLimiter: F[RateLimitterAlgebra[F]] = RedisRateLimiter[F](jedisResource, config.rateLimit, config.http.tokens)
  private val ratesHttpRoutes: HttpRoutes[F]  = new RatesHttpRoutes[F](ratesProgram, ratesLimiter).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
