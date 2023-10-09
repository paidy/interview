package forex.model.errors

import forex.model.domain.Rate
import org.http4s.{EntityEncoder, HttpVersion, MessageFailure, Response, Status}

import scala.util.control.NoStackTrace


object RateErrors {

  sealed trait RateError extends MessageFailure with NoStackTrace {
    def status: Status

    def toHttpResponse[F[_]](httpVersion: HttpVersion): Response[F] =
      Response(status, httpVersion).withEntity(message)(EntityEncoder.stringEncoder[F])
  }

  final case class RateNotFound(ratePair: Rate.Pair) extends RateError {
    override val message: String = s"Not found rate for currency pair (${ratePair.from} -> ${ratePair.to})"
    override val cause: Option[Throwable] = None
    override val status: Status = Status.NotFound
  }

  final case class OneFrameCallFailed(err: Throwable) extends RateError {
    override val message: String = s"Error happens during One-Frame service call, error: ${err.getMessage}"
    override val cause: Option[Throwable] = Some(err)
    override val status: Status = Status.ServiceUnavailable
  }
}
