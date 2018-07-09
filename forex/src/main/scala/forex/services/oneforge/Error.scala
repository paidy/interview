package forex.services.oneforge

import scala.util.control.NoStackTrace

sealed trait Error extends Throwable with NoStackTrace
object Error {
  final case object Generic extends Error
  final case class System(underlying: Throwable) extends Error

  final case class UnrecognizableApiResponse(underlying: Throwable) extends Error

  final case class UnsupportedContentType(contentType: String) extends Error

  final case class ApiException(statusCode: Int, message: String) extends Error

  final case class UnparsableQuoteResponse(msg: String) extends Error
}
