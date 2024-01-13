package forex.http.ratelimitter

import cats.effect.{Async, Resource}
import cats.implicits._
import forex.config.RateLimitConfig
import forex.http.ratelimitter.error.{InvalidToken, RateLimitError, TokenExhausted}
import forex.http.ratelimitter.interpreters.RateLimitterAlgebra
import redis.clients.jedis.Jedis
import redis.clients.jedis.params.SetParams
class RedisRateLimiter[F[_]: Async](redisClient: Resource[F, Jedis], config: RateLimitConfig)
    extends RateLimitterAlgebra[F] {
  private val rateLimitPrefix = "rate_limit"

  def isAllowed(token: String): F[RateLimitError Either RateLimiterSuccess] = {
    val key = s"$rateLimitPrefix:$token"
    redisClient.use { redis =>
      Async[F].delay(Option(redis.get(key))).map {
        case Some(count) if count.toIntOption.exists(_ >= config.limitPerToken) =>
          Left(TokenExhausted(s"Token $token exhausted the limit ${config.limitPerToken}"))
        case Some(count) if count.toIntOption.exists(_ < config.limitPerToken) =>
          Right(true)
        case _ => Left(InvalidToken(s"Invalid Token"))
      }
    }
  }

  override def increment(key: String): F[Unit] = {
    val rateLimitKey = s"$rateLimitPrefix:$key"
    redisClient.use { redis =>
      Async[F].delay(redis.incr(rateLimitKey)).void
    }
  }
}

object RedisRateLimiter {
  def apply[F[_]: Async](redisClient: Resource[F, Jedis],
                         config: RateLimitConfig,
                         keys: List[String]): F[RateLimitterAlgebra[F]] =
    {
      redisClient.use { redis =>
        keys.traverse_ { key =>
          val rateLimitKey = s"rate_limit:$key"
          Async[F].delay(Option(redis.get(rateLimitKey)).flatMap(_.toIntOption)).flatMap {
            case Some(_) => Async[F].pure("")
            case _ =>
              Async[F].delay{
                redis.set(rateLimitKey, "0", new SetParams().ex(config.windowSize.toSeconds.intValue()))//TODO: this will fail
              }
          }
        }
      }.as(new RedisRateLimiter[F](redisClient, config))
    }

}
