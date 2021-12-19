package forex

import cats.effect._
import forex.config._
import fs2.Stream
import org.http4s.blaze.server.BlazeServerBuilder

object Main extends IOApp.Simple {
  override def run: IO[Unit] = new Application[IO].stream().compile.drain
}

class Application[F[_] : Async] {

  def stream(): Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      module = new Module[F](config)

      _ <- BlazeServerBuilder[F]
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve

    } yield ()

}
