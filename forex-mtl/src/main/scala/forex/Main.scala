package forex

import cats.Parallel

import scala.concurrent.ExecutionContext
import cats.effect._
import dev.profunktor.redis4cats.log4cats._
import forex.config._
import forex.resources.AppResources
import fs2.Stream
import org.http4s.server.blaze.BlazeServerBuilder
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream(executionContext).compile.drain.as(ExitCode.Success)

}

class Application[F[_]: ConcurrentEffect: Timer: Logger: ContextShift: Parallel] {

  def stream(ec: ExecutionContext): Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      resources <- AppResources.stream[F](config, ec)
      module = new Module[F](config, resources)
      _ <- BlazeServerBuilder[F](ec)
            .bindHttp(config.httpServer.port, config.httpServer.host)
            .withHttpApp(module.httpApp)
            .serve
    } yield ()

}
