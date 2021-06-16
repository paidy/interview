package forex.client

object errors {
  sealed trait Error extends RuntimeException
  final case class OneFrameClientResponseError(error: String) extends Error
}
