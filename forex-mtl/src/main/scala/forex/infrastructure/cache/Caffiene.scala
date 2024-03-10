package forex.infrastructure.cache

import com.github.blemale.scaffeine.Scaffeine
import com.github.blemale.scaffeine.Cache
import scala.concurrent.duration.FiniteDuration

object Caffiene {
  def newInstance[K,V](expireTime: FiniteDuration, maxSize: Long): Cache[K,V] = {
    return Scaffeine()
      .recordStats()
      .expireAfterWrite(expireTime)
      .maximumSize(maxSize)
      .build[K,V]()
  }
}
