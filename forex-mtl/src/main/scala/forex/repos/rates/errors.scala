package forex.repos.rates

object errors {
  sealed trait Error

  object Error {
    final case class InvalidAddress(msg: String) extends Error
    final case class OneFrameQueryFailed(msg: String) extends Error
  }
}
