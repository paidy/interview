package forex

import cats.data.EitherT

import scala.concurrent.ExecutionContext
import cats.effect._
import forex.config._
import forex.module.{ ResourcesModule, RootModule }
import org.http4s.server.blaze.BlazeServerBuilder

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Config.read[IO]("app").flatMap {
      case Right(config) => runApp(config)
      case Left(ex)      => failApp(ex)
    }

  private def runApp(config: ApplicationConfig): IO[ExitCode] =
    new Application[IO].execute(executionContext, config)

  private def failApp(exception: Exception): IO[ExitCode] =
    IO.delay(println(s"application failed due to misconfiguration: \n$exception")).flatMap(_ => IO.pure(ExitCode.Error))
}

class Application[F[_]: ConcurrentEffect: Timer: ContextShift] {

  def execute(ec: ExecutionContext, config: ApplicationConfig): F[ExitCode] =
    ResourcesModule(config).resources.use { resources =>
      val module = RootModule(config, resources.transactor, resources.streamBackend)

      val program =
        EitherT(
          ConcurrentEffect[F].race(
            BlazeServerBuilder[F](ec)
              .bindHttp(config.http.port, config.http.host)
              .withHttpApp(module.httpApp)
              .serve
              .compile
              .drain,
            module.scheduler.compile.drain
          )
        ).leftMap(_ => ExitCode.Error)

      program.fold(identity[ExitCode], _ => ExitCode.Success)
    }

}
