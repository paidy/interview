package forex

import cats.effect.{Concurrent, ConcurrentEffect, Timer}
import forex.config.{ApplicationConfig, OneFrameConfig}
import forex.http.rates.RatesHttpRoutes
import forex.services.RatesService
import forex.programs._
import forex.services.rates.interpreters.OneFrameInterpreter
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}

import scala.annotation.nowarn

@nowarn("cat=unused")
class Module[F[_]: Concurrent: Timer :ConcurrentEffect](config: ApplicationConfig) {
  private val oneFrameConfig: OneFrameConfig = config.oneFrame
  private val oneFrameApiUri: Uri = Uri.fromString(oneFrameConfig.uri).getOrElse(
    throw new Exception("Invalid OneFrame API URI provided")
  )
  private val oneFrameToken: String = oneFrameConfig.token

  private val ratesService: RatesService[F] = new OneFrameInterpreter[F](oneFrameApiUri, oneFrameToken)

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
