package forex.services.healthcheck

import scala.concurrent.duration._
import cats.effect._
import cats.effect.implicits._
import cats.syntax.all._
import cats.effect.Concurrent
import dev.profunktor.redis4cats.RedisCommands

import forex.domain.HealthCheck._


object Interpreter {

  def make[F[_]: Concurrent: Timer](
      redis: RedisCommands[F, String, String]
  ): Algebra[F] =
    new Algebra[F] {
      val redisHealth: F[RedisStatus] =
        redis.ping
          .map(_.nonEmpty)
          .timeout(1.second)
          .map(bool => if (bool) Status.OK else Status.Unreachable)
          .orElse(Status.Unreachable.pure[F].widen)
          .map(RedisStatus.apply)

      override def status: F[AppStatus] = redisHealth.map(AppStatus)
    }

}
