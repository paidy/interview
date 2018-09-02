package forex.config

import cats.effect.Sync
import fs2.Stream

object Config {

  def stream[F[_]: Sync](path: String): Stream[F, ApplicationConfig] =
    Stream.eval(Sync[F].delay(pureconfig.loadConfigOrThrow[ApplicationConfig](path)))

}
