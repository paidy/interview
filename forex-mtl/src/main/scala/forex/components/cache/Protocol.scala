package forex.components.cache

import forex.components.cache.redis.Redis

object Protocol {
  def redis(segment: String) : Algebra = new Redis(segment)
}
