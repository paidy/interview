package forex.config

import cats.effect.Sync
import fs2.Stream
import pureconfig.generic.auto._

object Config {

  def stream[F[_]: Sync](path: String): Stream[F, ApplicationConfig] =
    Stream.eval(Sync[F].delay(pureconfig.loadConfigOrThrow[ApplicationConfig](path)))

}
