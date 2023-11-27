package forex.programs.rates

import forex.services.rates.errors.{ Error => RatesServiceError }

object errors {

  abstract class Error(msg: String) extends Exception(msg) {}
  object Error {
    final case class RateLookupFailed(msg: String) extends Error(msg)
  }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.OneFrameLookupFailed(msg) => Error.RateLookupFailed(msg)
  }
}
