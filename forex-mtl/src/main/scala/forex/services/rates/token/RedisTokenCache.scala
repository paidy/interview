package forex.services.rates.token

import cats.effect.{Async, Resource}
import cats.implicits.toFunctorOps
import forex.config.Token
import forex.services.rates.token.RedisTokenCache.oneFrameTokensKey
import redis.clients.jedis.Jedis

import scala.jdk.CollectionConverters._
class RedisTokenCache[F[_]: Async](redisClient: Resource[F, Jedis], tokenConfig: Token) extends TokenCacheAlgebra[F] {

  override def getToken(): F[Option[String]] = redisClient.use { redis =>
    Async[F].delay {
      redis
        .hgetAll(oneFrameTokensKey)
        .asScala
        .toMap
        .filter {
          case (_, tokenUsed) => tokenUsed.toIntOption.exists(_ < tokenConfig.limitPerToken)
        }
        .keys
        .headOption
    }
  }

  override def incrementUsage(token: String): F[String] =
    redisClient.use { redis =>
      Async[F]
        .delay {
          redis.hincrBy(oneFrameTokensKey, token, 1)
        }
        .map { _ =>
          token
        }
    }
}
object RedisTokenCache {
  val oneFrameTokensKey = "oneframe.service.tokens"
  def apply[F[_]: Async](redisClient: Resource[F, Jedis], tokenConfig: Token): F[TokenCacheAlgebra[F]] =
    redisClient
      .use { redis =>
        Async[F].delay {
          if (!redis.exists(oneFrameTokensKey).booleanValue()) {
            val tokenUsage = tokenConfig.values.map(token => (token, "0")).toMap.asJava
            redis.hset(oneFrameTokensKey, tokenUsage)
            redis.expire(oneFrameTokensKey, tokenConfig.windowSize.toSeconds.intValue())
          }
        }
      }
      .as(new RedisTokenCache[F](redisClient, tokenConfig))
}
