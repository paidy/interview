package forex.thirdPartyApi.oneFrame

object errors {

  sealed trait OneFrameApiClientError

  object OneFrameApiClientError {
    final case class JsonDecodingError(msg: String) extends OneFrameApiClientError
  }

}
