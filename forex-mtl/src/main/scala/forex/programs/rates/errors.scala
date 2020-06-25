package forex.programs.rates

import cats.Show
import forex.services.rates.errors.{Error => RatesServiceError}

object errors {

  sealed trait Error extends Exception{
    def msg:String
  }
  object Error {
    final case class RateLookupFailed(msg: String) extends Error
    final case class InvalidInput(msg: String) extends Error
  }

  def toProgramError(error: RatesServiceError): Error = error match {
    case RatesServiceError.OneFrameLookupFailed(msg) => Error.RateLookupFailed(msg)
  }

  def toInvalidInputError(message: String): Error = Error.InvalidInput(message)

  implicit val showMessage : Show[Error] = Show.show(_.msg)
}
