package forex

import cats.effect.{ ConcurrentEffect, Resource, Timer }
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import forex.repos.{ RatesRepo, RatesRepos }
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.{ AutoSlash, Timeout }

import scala.concurrent.ExecutionContext

class Module[F[_]: Timer: ConcurrentEffect](ec: ExecutionContext, config: ApplicationConfig) {

  // we share the same ec for the sake of simplicity
  private val httpClient = BlazeClientBuilder[F](ec).resource

  private val ratesRepo: RatesRepo[F] = RatesRepos.default[F](httpClient: Resource[F, Client[F]], config.oneFrame)

  private val ratesIngestor: RatesIngestor[F] = RatesIngestors.default[F](ratesRepo, config.ratesIngestor)

  private val ratesService: RatesService[F] = RatesServices[F](ratesIngestor)

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

  val refreshTask: F[Unit] = ratesIngestor.refreshCache

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
