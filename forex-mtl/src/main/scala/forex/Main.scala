package forex

import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import cats.effect._
import forex.config._
import fs2.Stream
import org.http4s.server.blaze.BlazeServerBuilder

object Main extends IOApp.WithContext {

  implicit lazy val ec: ExecutionContext = executionContext
  implicitly[Timer[IO]]
  implicitly[ExecutionContext]

  private val threadMultiplier = 2

  override protected def executionContextResource: Resource[SyncIO, ExecutionContext] =
    Resource
      .make(SyncIO(Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors() * threadMultiplier)))(
        pool =>
          SyncIO {
            pool.shutdown()
        }
      )
      .map(ExecutionContext.fromExecutorService)

  override def run(args: List[String]): IO[ExitCode] =
    new Application[IO].stream(executionContext).compile.drain.as(ExitCode.Success)

}

class Application[F[_]: ConcurrentEffect: Timer] {
  def stream(ec: ExecutionContext): Stream[F, ExitCode] =
    for {
      config <- Config.stream("app")
      module = new Module[F](config)
      mainStream <- BlazeServerBuilder[F](ec)
                     .bindHttp(config.http.port, config.http.host)
                     .withHttpApp(module.httpApp)
                     .serve
                     .concurrently(module.oneFrameCache.scheduleCacheRefresh())
    } yield mainStream

}
