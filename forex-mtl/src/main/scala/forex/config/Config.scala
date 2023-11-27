package forex.config

import cats.effect.Sync

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Config {

  /**
    * @param path the property path inside the default configuration
    */
  def default[F[_]: Sync](path: String): F[ApplicationConfig] =
    Sync[F].delay(ConfigSource.default.at(path).loadOrThrow[ApplicationConfig])

}
