package forex.programs.rates

import forex.services.rates.errors.{ Error => RatesServiceError }

object errors {

  sealed trait Error extends Exception
  object Error {
    final case class RateLookupFailed(msg: String) extends Error
    final case class RateInvalidString(msg: String) extends Error
    final case class InvalidCurrencyString(msg: String) extends Error
  }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.OneFrameLookupFailed(msg) => Error.RateLookupFailed(msg)
    case RatesServiceError.JsonParsingFailed(msg) => Error.RateInvalidString(msg)
    case RatesServiceError.JsonDecodingFailed(msg) => Error.RateInvalidString(msg)
  }
}
