package forex

import scala.concurrent.ExecutionContext
import cats.effect._
import forex.config._
import fs2.Stream
import org.http4s.server.blaze.BlazeServerBuilder
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      blocker <- Blocker.apply[IO]
      backend <- AsyncHttpClientFs2Backend.resource[IO](blocker)
    } yield backend).use { backend =>
      new Application[IO].stream(executionContext, backend).compile.drain.as(ExitCode.Success)
    }

}

class Application[F[_]: ConcurrentEffect: Timer] {

  def stream(ec: ExecutionContext, backend: SttpBackend[F, _]): Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      module = new Module[F](config, backend)
      _ <- BlazeServerBuilder[F](ec)
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve
    } yield ()

}
