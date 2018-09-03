package forex.rates

object errors {

  sealed trait RateError extends Exception
  object RateError {
    final case class RemoteClientError(msg: String) extends RateError
  }

}
