package forex.services.rates

import cats.Applicative
import forex.config.{OneFrameConfig, RedisConfig}
import interpreters._

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()
  def live[F[_] : Applicative](oneFrameConfig: OneFrameConfig, redisConfig: RedisConfig): Algebra[F] = new OneFrameLive[F](oneFrameConfig, redisConfig)
}
