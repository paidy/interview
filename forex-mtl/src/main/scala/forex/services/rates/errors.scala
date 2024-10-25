package forex.services.rates

object errors {

  sealed trait Error extends Throwable
  object Error {
    final case class OneFrameLookupFailed(msg: String) extends Error

    final case class JsonParsingFailed(msg: String) extends Error

    final case class JsonDecodingFailed(str: String) extends Error
    final case class InvalidCurrency(str: String) extends Error
  }

}
