package forex.services.rates

object errors {

  sealed trait RateError
  object RateError {
    final case class RemoteClientError(msg: String) extends RateError
  }

}
