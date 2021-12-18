package forex

import scala.concurrent.ExecutionContext

import cats.effect._
import forex.config._
import fs2.Stream
import org.http4s.server.blaze.BlazeServerBuilder

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream(executionContext).compile.drain.as(ExitCode.Success)

}

class Application[F[_] : Dispatcher: Temporal] {
  // maintain Ref (atomic) for the state of the cache, Deferred for the polling action to set the Ref
  /*ctr <- IO.ref(0)

  wait = IO.sleep(1.second)
  poll = wait *> ctr.get

  _ <- poll.flatMap(IO.println(_)).foreverM.start
  _ <- poll.map(_ % 3 == 0).ifM(IO.println("fizz"), IO.unit).foreverM.start
  _ <- poll.map(_ % 5 == 0).ifM(IO.println("buzz"), IO.unit).foreverM.start

  _ <- (wait *> ctr.update(_ + 1)).foreverM.void*/
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
