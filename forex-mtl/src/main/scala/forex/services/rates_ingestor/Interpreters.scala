package forex.services.rates_ingestor
import cats.Monad
import cats.effect.Timer
import forex.config.RatesIngestorConfig
import forex.repos
import forex.services.rates_ingestor.interpreters.RatesIngestor

object Interpreters {
  def default[F[_]: Monad: Timer](ratesDAO: repos.rates.Algebra[F], settings: RatesIngestorConfig) =
    new RatesIngestor[F](ratesDAO, settings)
}
