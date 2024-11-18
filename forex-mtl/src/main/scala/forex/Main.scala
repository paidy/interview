package forex

import scala.concurrent.ExecutionContext
import cats.effect._
import forex.config._
import fs2.Stream
import org.http4s.blaze.server.BlazeServerBuilder

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val startupTask = IO {StartupTasks.process()}
    val app: IO[ExitCode] = new Application[IO].stream(executionContext).compile.drain.as(ExitCode.Success)
    for {
      _ <- startupTask.start //This will run async and starts another task after 4minutes to refresh cache...
      exitCode <- app //This will run the app in current thread
    } yield { exitCode}
  }
}

class Application[F[_]: ConcurrentEffect: Timer] {

  def stream(ec: ExecutionContext): Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      module = new Module[F](config)
      _ <- BlazeServerBuilder[F](ec)
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve
    } yield ()
}
