package forex.resources

import fs2.Stream
import cats.Parallel
import cats.syntax.all._
import cats.effect.{ConcurrentEffect, ContextShift, Resource}
import org.http4s.client.Client
import org.http4s.client.blaze._
import org.typelevel.log4cats.Logger
import dev.profunktor.redis4cats.effect.Log
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import forex.config.{ApplicationConfig, HttpClientConfig, RedisConfig}

import scala.concurrent.ExecutionContext


sealed abstract class AppResources[F[_]](
  val client: Client[F],
  val redis: RedisCommands[F, String, String]
)

object AppResources {

  def stream[F[_]: ConcurrentEffect: Logger: ContextShift: Log: Parallel](
    config: ApplicationConfig,
    ec: ExecutionContext
  ): Stream[F, AppResources[F]] = {

    def mkHttpClient(config: HttpClientConfig, ec: ExecutionContext): Resource[F, Client[F]] =
      BlazeClientBuilder[F](ec)
        .withResponseHeaderTimeout(config.timeout)
        .withIdleTimeout(config.idleTimePool)
        .resource

    def checkRedisConnection(
      redis: RedisCommands[F, String, String]
    ): F[Unit] =
      redis.info.flatMap {
        _.get("redis_version").traverse_ { v =>
          Logger[F].info(s"Connected to Redis $v")
        }
      }

    def mkRedisResource(config: RedisConfig): Resource[F, RedisCommands[F, String, String]] =
      Redis[F].utf8(s"${config.host}:${config.port}").evalTap(checkRedisConnection)

    Stream.resource(
      (
        mkHttpClient(config.httpClient, ec),
        mkRedisResource(config.redis)
      ).parMapN(new AppResources[F](_, _) {})
    )
  }
}
