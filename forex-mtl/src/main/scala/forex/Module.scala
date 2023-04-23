package forex

import cats.effect.{ Concurrent, Timer }
import forex.config.ApplicationConfig
import forex.http.external.oneframe.{ RateClient, RateHttpClient }
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import org.http4s._
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.server.middleware.{ AutoSlash, Timeout }

class Module[F[_]: Concurrent: Timer](config: ApplicationConfig, client: Client[F]) {

  private val oneFrameToken: String = "10dc303535874aeccc86a8251e6992f5"

  private val ratesClient: RateClient[F] = RateHttpClient[F](oneFrameToken, client)

  private val ratesService: RatesService[F] = RatesServices.http[F](ratesClient)

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
