package forex

import cats.effect.{ConcurrentEffect, Timer}
import forex.config.{ApplicationConfig, Dummy, Simple}
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import forex.services.rates.Interpreters
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}

class Module[F[_]: ConcurrentEffect: Timer](config: ApplicationConfig) {

  private val ratesService: RatesService[F] = config.source match {
    case Dummy => Interpreters.dummy()
    case Simple(uri, token) => Interpreters.simple(uri.getUri, token)
  }

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
