package forex.components.cache.redis

abstract class RedisHelper {
  def getSegmentPrefixedKey(segement: String, key:String): String
}

object Segments {
  final val forexRates = "FOREX_RATES"
}

object ForexRedisHelper extends RedisHelper {
  override def getSegmentPrefixedKey(segment: String, key: String): String = segment ++ "::" ++ key

  def getFormattedKey(from: String, to: String): String = {
    from + "&" + to
  }
}
