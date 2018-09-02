package forex.rates

object errors {

  sealed trait RateError extends Exception
  final case class RemoteClientError(msg: String) extends RateError

}
