package forex

import cats.effect._
/*import forex.config._
import fs2.Stream
import org.http4s.blaze.server.BlazeServerBuilder*/

import scala.concurrent.duration.DurationInt

object Main extends IOApp.Simple {

  override def run(): IO[Unit] = //new Application[IO].stream().compile.drain
    for {
      ctr <- IO.ref(0)

      wait = IO.sleep(1.second)
      poll = wait *> ctr.get

      _ <- poll.flatMap(IO.println(_)).foreverM.start
      _ <- poll.map(_ % 3 == 0).ifM(IO.println("fizz"), IO.unit).foreverM.start
      _ <- poll.map(_ % 5 == 0).ifM(IO.println("buzz"), IO.unit).foreverM.start

      _ <- (wait *> ctr.update(_ + 1)).foreverM.void
    } yield ()


}
/*
class Application[F[_] : Concurrent : Temporal : Async] {
  // maintain Ref (atomic) for the state of the cache, Deferred for the polling action to set the Ref

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
*/