package forex

import cats.effect.{ Concurrent, Timer }
import forex.client.OneFrameClient
import forex.config.ApplicationConfig
import forex.domain.Rate
import forex.http.rates.RatesHttpRoutes
import forex.programs._
import forex.services._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{ AutoSlash, Timeout }
import scalacache.Cache
import scalacache.caffeine.CaffeineCache
import sttp.client.{ HttpURLConnectionBackend, Identity, NothingT, SttpBackend }

class Module[F[_]: Concurrent: Timer](config: ApplicationConfig) {

  private val rateCache: Cache[Rate] = CaffeineCache[Rate]

  private implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

  private lazy val oneFrameClient: OneFrameClient[F] = OneFrameClient.OneFrameHttpClient(config.oneFrame)

  val ratesService: RatesService[F] = RatesServices.live[F](oneFrameClient, rateCache, config.cache, config.scheduler)

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
