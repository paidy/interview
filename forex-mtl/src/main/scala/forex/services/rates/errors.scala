package forex.services.rates

object errors {

  sealed trait Error
  object Error {
    final case class OneFrameLookupFailed(code:String, msg: String) extends Error
    final case class RateLookupFailed(code:String, msg: String) extends Error
  }

}
