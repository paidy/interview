package forex.repo

import com.redis.RedisClient
import forex.config.RedisConfig
import forex.domain.Rate
import io.circe.Encoder

class RedisCache(config: RedisConfig) {
  private val conn = new RedisClient(config.host, config.port)

  val pairToKey = (pair: Rate.Pair) => s"${pair.to}${pair.from}"

  def get(pair: Rate.Pair): Option[Rate] = conn.get(pairToKey(pair)) match {
    case Some(jsonString) => io.circe.parser.decode[Rate](jsonString) match {
      case Right(data) => Some(data)
      case Left(_) => None
    }
    case None => None
  }

  def setOne(rate: Rate): Boolean = conn.set(
    pairToKey(rate.pair),
    Encoder[Rate].apply(rate).noSpaces,
    expire = config.expire
  )

  def setAll(rates: List[Rate]): Boolean = rates.map(rate => setOne(rate)).forall(identity)
}

object RedisCache {
  // Singleton instance
  @volatile private var instance: RedisCache = _

  // Method to access the singleton instance
  def getInstance(config: RedisConfig): RedisCache = {
    if (instance == null) {
      synchronized {
        if (instance == null) {
          instance = new RedisCache(config)
        }
      }
    }
    instance
  }
}