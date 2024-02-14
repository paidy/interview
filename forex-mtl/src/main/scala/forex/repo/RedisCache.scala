package forex.repo

import com.redis.RedisClient
import forex.config.RedisConfig
import forex.domain.Rate
import forex.domain.Rate.Pair
import io.circe.Encoder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RedisCache(config: RedisConfig) {
  private val conn = new RedisClient(config.host, config.port)

  def get(pair: Rate.Pair): Option[Rate] = conn.get(Pair.stringify(pair)) match {
    case Some(jsonString) => io.circe.parser.decode[Rate](jsonString) match {
      case Right(data) => Some(data)
      case Left(_) => None
    }
    case None => None
  }

  def setOne(rate: Rate): Future[Boolean] = Future {
    conn.set(
      Pair.stringify(rate.pair),
      Encoder[Rate].apply(rate).noSpaces,
      expire = config.expire
    )
  }

  def setAll(rates: List[Rate]): Future[Boolean] = {
    val setResults: List[Future[Boolean]] = rates.map(rate => setOne(rate))
    Future.sequence(setResults).map(_.forall(identity))
  }
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
