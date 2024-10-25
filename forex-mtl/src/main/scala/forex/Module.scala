package forex

import cats.effect.{ Concurrent, Timer }
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{ AutoSlash, Timeout }
import sttp.client3.SttpBackend

class Module[F[_]: Concurrent: Timer](config: ApplicationConfig, backend: SttpBackend[F, _]) {

  private val storageService: StorageService[F] = StorageService.inMemory[F](config.storage)

  private val ratesService: RatesService[F] = RatesServices.live[F](config.provider, backend)

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService, storageService)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
