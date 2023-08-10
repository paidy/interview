package forex.programs.rates

import forex.services.rates.errors.{ Error => RatesServiceError }

object errors {

  sealed trait Error extends Exception {
    def msg: String
  }
  object Error {
    final case class RateLookupFailed(msg: String) extends Error
  }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.OneFrameLookupFailed(msg) => Error.RateLookupFailed(msg)
    case RatesServiceError.ApiRequestFailed(msg)     => Error.RateLookupFailed(msg)
    case RatesServiceError.ParseFailure(msg)         => Error.RateLookupFailed(msg)
  }
}
