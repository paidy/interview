package forex.services.rates

object errors {

  sealed trait RateServiceError
  object RateServiceError {
    final case class OneFrameLookupFailed(msg: String) extends RateServiceError
  }

}
