package forex

import cats.effect.{ Concurrent, Timer }
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import forex.cache._
import forex.client._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{ AutoSlash, Timeout }
import sttp.client.{ HttpURLConnectionBackend, Identity, NothingT, SttpBackend }

class Module[F[_]: Concurrent: Timer](config: ApplicationConfig) {

  private val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

  private val oneFrameClient: OneFrameClient[F] = OneFrameClient[F](backend, config.oneFrameConfig)
  val oneFrameCache: OneFrameCache[F]           = OneFrameCache[F](oneFrameClient, config.oneFrameConfig.scheduleTime)

  private val ratesService: RatesService[F] = RatesServices[F](oneFrameCache, config.ratesExpiration)

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService)

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
