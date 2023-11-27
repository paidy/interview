package forex.repos.rates

import cats.effect.{ Resource, Sync }
import forex.config.OneFrameConfig
import forex.repos.rates.interpreters.OneFrameDAO
import org.http4s.client.Client

object Interpreters {
  def default[F[_]: Sync](httpClient: Resource[F, Client[F]], settings: OneFrameConfig) =
    new OneFrameDAO[F](httpClient, settings)
}
