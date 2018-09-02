package forex

import cats.data.OptionT
import cats.syntax.functor._
import cats.effect._
import forex.config._
import fs2.Stream
import org.http4s.server.blaze.BlazeBuilder

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream.compile.drain.as(ExitCode.Success)

}

class Application[F[_]: ConcurrentEffect: Timer] {

  def stream: Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      module = new Module[F](config)
      _ <- BlazeBuilder[F]
                    .bindHttp(config.http.port, config.http.host)
                    .mountService(module.httpApp.mapF(OptionT.liftF(_)))
                    .serve
    } yield ()

}
