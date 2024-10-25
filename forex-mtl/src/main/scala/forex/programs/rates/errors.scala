package forex.programs.rates

import forex.services.rates.errors.{ RateServiceError => RatesServiceError }

object errors {

  sealed trait Error extends Exception
  object Error {
    final case class RateLookupFailed(msg: String) extends Error
  }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.OneFrameLookupFailed(msg) => Error.RateLookupFailed(msg)
    case RatesServiceError.OneFrameAPIRequestFailed(msg) => Error.RateLookupFailed(msg)
    case RatesServiceError.OneFrameParseRatesFailed(msg) => Error.RateLookupFailed(msg)
  }
}
