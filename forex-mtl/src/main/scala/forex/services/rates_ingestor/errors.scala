package forex.services.rates_ingestor

import forex.domain.Rate

object errors {
  sealed trait Error {
    def msg: String
  }

  object Error {
    final case class PairIsAbsent(pair: Rate.Pair) extends Error {
      override val msg: String = s"$pair can not be found in cache, it's probably because of the fail ingestion"
    }
  }
}
