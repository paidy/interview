package forex.config

import cats.data.EitherT
import cats.effect.Sync
import cats.implicits.toBifunctorOps
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException
import pureconfig.generic.auto._

object Config {

  /**
    * @param path the property path inside the default configuration
    */
  def read[F[_]: Sync](path: String): F[ConfigReaderException[ApplicationConfig] Either ApplicationConfig] =
    EitherT(
      Sync[F]
        .delay(ConfigSource.default.at(path).load[ApplicationConfig].leftMap(ConfigReaderException[ApplicationConfig]))
    ).value

}
