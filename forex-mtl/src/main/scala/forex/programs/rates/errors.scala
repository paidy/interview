package forex.programs.rates

import forex.services.rates.errors.{ RateError => ServiceRateError}

object errors {

  sealed trait RateError extends Exception
  object RateError {
    final case class RemoteClientError(msg: String) extends RateError
  }

  def toProgramError(error: ServiceRateError): RateError = error match {
    case ServiceRateError.RemoteClientError(msg) => RateError.RemoteClientError(msg)
  }
}
