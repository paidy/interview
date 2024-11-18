package forex.components.cache.redis

import com.redis.RedisClient
import forex.components.cache.Algebra
import forex.programs.rates.ErrorCodes
import forex.services.rates.errors._

class Redis(segment: String) extends Algebra {

  private[redis] lazy val redisClient: RedisClient = new RedisClient("localhost", 6379)

  override def put[A](key: String, value: A): Boolean = {
    redisClient.set(key = ForexRedisHelper.getSegmentPrefixedKey(segment, key), value = value)
  }

  override def get(key:String): Either[Error, String] = {
    redisClient.get(ForexRedisHelper.getSegmentPrefixedKey(segment, key)) match {
      case Some(value) => Right(value).withLeft[Error]
      case None => Left(Error.RateLookupFailed(ErrorCodes.cacheFetchFailed, "Value Not Found In Cache"))
    }

  }

}
