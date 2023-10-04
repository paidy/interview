package forex

import cats.data.OptionT
import cats.effect.{Async, Sync}
import com.typesafe.scalalogging.LazyLogging
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, ErrorAction, ErrorHandling, Timeout}

class Module[F[_]: Async](config: ApplicationConfig) extends LazyLogging{

  private val ratesService: RatesService[F] = RatesServices.dummy[F]

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

  private val logError: (Throwable, => String) => OptionT[F,Unit] = (t, msg) =>
    OptionT(Sync[F].delay {
      logger.error(msg, t)
      Some(())
    })

  private val http: HttpRoutes[F] = ErrorHandling.Recover.total(
    ErrorAction.log(ratesHttpRoutes, logError, logError)
  )

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
