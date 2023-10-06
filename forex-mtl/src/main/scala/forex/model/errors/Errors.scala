package forex.model.errors

object Errors {

  sealed trait Error extends Exception

  object Error {
    final case class RateLookupFailed(msg: String) extends Error
    final case class OneFrameLookupFailed(msg: String) extends Error
    final case class RatesServiceError(msg: String) extends Error
  }

  def toProgramError(error: Error.OneFrameLookupFailed): Error = error match {
    case Error.OneFrameLookupFailed(msg) => Error.RateLookupFailed(msg)
  }
}
