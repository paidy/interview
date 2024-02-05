package forex.services.cache.interpreters

import com.redis.RedisClient
import forex.config.RedisConfig
import forex.domain.{Price, Rate, Timestamp}
import forex.services.cache.Algebra


class RedisCache(config: RedisConfig) extends Algebra {

  val rc = new RedisClient(config.host, config.port)

  val pairToKey = (pair: Rate.Pair) => s"${pair.to}${pair.from}"
  override def get(pair: Rate.Pair): Option[Rate] = Some(Rate(pair, Price(0.12), Timestamp.now))

  override def setOne(rate: Rate): Boolean = rc.set(pairToKey(rate.pair), rate, expire = config.expire)

  override def setAll(rates: List[Rate]): Boolean = rates.map(setOne).head // TODO: fix the implementation

}
