package forex.services.rates

import forex.services.rates_ingestor.errors.{ Error => IngestorError }

object errors {

  sealed trait Error
  object Error {
    final case class OneFrameLookupFailed(msg: String) extends Error
  }

  def toRateServiceError(error: IngestorError): Error = error match {
    case e: IngestorError.PairIsAbsent => Error.OneFrameLookupFailed(e.msg)
  }
}
