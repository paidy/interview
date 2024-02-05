package forex.services.cache


import forex.config.RedisConfig
import forex.services.cache.interpreters.RedisCache

object Interpreters {

  def RedisCache(config: RedisConfig) = new RedisCache(config)
}
