package forex

import scala.concurrent.ExecutionContext
import cats.effect._
import forex.config._
import forex.resources.AppResources
import fs2.Stream
import org.http4s.server.blaze.BlazeServerBuilder

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream(executionContext).compile.drain.as(ExitCode.Success)

}

class Application[F[_]: ConcurrentEffect: Timer] {

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
