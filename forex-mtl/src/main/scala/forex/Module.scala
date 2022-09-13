package forex

import cats.effect.{Concurrent, Timer}
import cats.implicits.toFunctorOps
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.programs._
import forex.services._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}

class Module[F[_]: Concurrent: Timer](config: ApplicationConfig) {

  private val ratesService: F[RatesService[F]] = RatesServices.cached[F]

  private val ratesProgram: F[RatesProgram[F]] = ratesService.map { service =>
    RatesProgram[F](service)
  }

  private val ratesHttpRoutes: F[HttpRoutes[F]] = ratesProgram.map { program =>
    new RatesHttpRoutes[F](program).routes
  }

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

  private val http: F[HttpRoutes[F]] = ratesHttpRoutes

  val httpApp: F[HttpApp[F]] = http.map { routes =>
    appMiddleware(routesMiddleware(routes).orNotFound)
  }

}
