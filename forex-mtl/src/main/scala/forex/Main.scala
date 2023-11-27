package forex

import scala.concurrent.ExecutionContext
import cats.effect._
import cats.implicits._
import forex.config._
import org.http4s.server.blaze.BlazeServerBuilder

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].start(executionContext).as(ExitCode.Success)

}

class Application[F[_]: ConcurrentEffect: Timer] {

  def start(ec: ExecutionContext): F[Unit] =
    for {
      config <- Config.default("app")
      module = new Module[F](ec, config)
      _ <- implicitly[ConcurrentEffect[F]].start(module.refreshTask)
      _ <- BlazeServerBuilder[F](ec)
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve
            .compile
            .drain
    } yield ()
}
