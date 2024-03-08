package forex.thirdPartyApi.oneFrame

object errors {

  sealed trait OneFrameApiClientError {
    val msg: String
  }

  object OneFrameApiClientError {
    final case class JsonDecodingError(msg: String) extends OneFrameApiClientError
    final case class RequestError(msg: String) extends OneFrameApiClientError
  }

}
