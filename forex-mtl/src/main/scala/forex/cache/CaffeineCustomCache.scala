package forex.cache

import com.github.benmanes.caffeine.cache._

import java.util.concurrent.TimeUnit

class CaffeineCustomCache[K, V](private val capacity: Long = 10000, private val ttl: Long = 1) extends CustomCache[K, V] {
  private val cache: Cache[K, V] = Caffeine
    .newBuilder()
    .maximumSize(capacity)
    .expireAfterWrite(ttl, TimeUnit.SECONDS)
    .build()

  override def put(key: K, value: V): Unit = {
    cache.put(key, value)
  }

  override def get(key: K): Option[V] = {
    Option(cache.getIfPresent(key))
  }
}


object CaffeineCustomCache {
  def apply[K, V](capacity: Long, ttl: Long): CaffeineCustomCache[K, V] =
    new CaffeineCustomCache[K, V](capacity, ttl)
}