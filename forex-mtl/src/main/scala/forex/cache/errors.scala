package forex.cache
object errors {

  sealed trait Error
  object Error {
    final case class KeyNotFoundInCache(error: String) extends Error
    final case class CacheNotReachable(error: String) extends Error
  }

}
