package forex.module

import cats.effect.{ Concurrent, Timer }
import doobie.util.transactor.Transactor
import forex.clients.{ OneFrameClient, OneFrameClients }
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.persistence.{ DatabaseExceptionHandler, ExceptionHandler, RatesRepository }
import forex.programs._
import forex.scheduler.SchedulerService
import forex.services._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{ AutoSlash, Timeout }
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.SttpBackend

class RootModule[F[_]: Concurrent: Timer](config: ApplicationConfig,
                                          transactor: Transactor[F],
                                          backend: SttpBackend[F, Fs2Streams[F]]) {

  private implicit val transactorImplicit: Transactor[F]  = transactor
  private implicit val exceptionHandler: ExceptionHandler = new DatabaseExceptionHandler
  private val repo: RatesRepository[F]                    = RatesRepository()

  private val ratesService: RatesService[F] = RatesServices.live[F](repo)

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService)

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = { http: HttpRoutes[F] =>
    AutoSlash(http)
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes

  private val oneFrameClient: OneFrameClient[F] = OneFrameClients.live(config.oneFrame, backend)

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

  val scheduler: fs2.Stream[F, Int] = SchedulerService(config.cache, repo, oneFrameClient).scheduler

}

object RootModule {

  def apply[F[_]: Concurrent: Timer](config: ApplicationConfig,
                                     transactor: Transactor[F],
                                     backend: SttpBackend[F, Fs2Streams[F]]) =
    new RootModule[F](config, transactor, backend)

}
