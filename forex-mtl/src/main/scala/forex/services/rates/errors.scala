package forex.services.rates

object errors {

  sealed trait Error
  object Error {
    final case class OneFrameLookupFailed(msg: String) extends Error
    final case class ApiRequestFailed(msg: String) extends Error
    final case class ParseFailure(msg: String) extends Error
  }

}
