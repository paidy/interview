package forex

import cats.effect.{Concurrent, Timer}
import forex.client.algebra.OneForgeClient
import forex.client.interpreters.OneForgeDummy
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.rates.algebra.Rates
import forex.rates.program.RatesProgram
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, CORS, Timeout}

class Module[F[_]: Concurrent: Timer](config: ApplicationConfig) {

  private val oneForgeClient: OneForgeClient[F] = new OneForgeDummy[F]

  private val rates: Rates[F] = new RatesProgram[F](oneForgeClient)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](rates).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    } andThen { http: HttpRoutes[F] =>
      CORS(http, CORS.DefaultCORSConfig)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
