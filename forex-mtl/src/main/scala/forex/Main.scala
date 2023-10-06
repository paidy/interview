package forex

import cats.effect._
import forex.model.config.Config
import fs2.Stream
import org.http4s.blaze.server.BlazeServerBuilder

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream().compile.drain.as(ExitCode.Success)

}

class Application[F[_]: Async] {

  def stream(): Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      module = new Module[F](config)
      _ <-BlazeServerBuilder[F]
        .bindHttp(config.http.port, config.http.host)
        .withHttpApp(module.httpApp)
        .serve
        .mergeHaltBoth(module.ratesRefresh)
    } yield ()

}
