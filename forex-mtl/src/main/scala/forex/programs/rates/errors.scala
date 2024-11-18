package forex.programs.rates

import forex.services.rates.errors.{ Error => RatesServiceError }

object errors {

  class Error(code: String, message: String) extends Exception {
    override def toString(): String = s"Code: $code, Message: $message"
  }
  object Error {
    final case class RateLookupFailed(code:String, msg: String) extends Error(code, msg)
  }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.OneFrameLookupFailed(code, msg) => Error.RateLookupFailed(code, msg)
    case RatesServiceError.RateLookupFailed(code, msg) => Error.RateLookupFailed(code, msg)
  }
}
