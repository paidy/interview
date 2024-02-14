package forex.services.rates

object errors {

  sealed trait RateServiceError
  object RateServiceError {
    final case class OneFrameLookupFailed(msg: String) extends RateServiceError
    final case class OneFrameAPIRequestFailed(msg: String) extends RateServiceError
    final case class OneFrameParseRatesFailed(msg: String) extends RateServiceError
  }

}
