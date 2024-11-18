package forex.programs.rates

import forex.services.rates.errors.{ Error => RatesServiceError }

object errors {

  sealed trait Error extends Exception
  object Error {
    final case class RateLookupFailed(code:String, msg: String) extends Error
  }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.OneFrameLookupFailed(code, msg) => Error.RateLookupFailed(code, msg)
    case RatesServiceError.RateLookupFailed(code, msg) => Error.RateLookupFailed(code, msg)
  }
}
