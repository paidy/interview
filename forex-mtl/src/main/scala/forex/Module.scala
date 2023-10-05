package forex

import cats.arrow.FunctionK
import cats.effect.{Async, Sync}
import com.typesafe.scalalogging.LazyLogging
import forex.config.ApplicationConfig
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, ErrorAction, ErrorHandling, Logger, Timeout}


class Module[F[_]: Async](config: ApplicationConfig) extends LazyLogging{

  private val ratesService: RatesService[F] = RatesServices.dummy[F]

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService, config.oneFrame)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val reqResLogger: TotalMiddleware = { http: HttpApp[F] =>
    Logger[F, F] (logHeaders = true, logBody = true, FunctionK.id) (http)
  }

  private val logError: (Throwable, => String) => F[Unit] = { (t, msg) =>
    Sync[F].delay { logger.error(msg, t) }
  }

  private val errorLogger: TotalMiddleware = { http: HttpApp[F] =>
    ErrorHandling.Recover.total(ErrorAction.log(http, logError, logError))
  }

  private val apiTimeout: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    reqResLogger(errorLogger(apiTimeout(http)))
  }

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(ratesHttpRoutes).orNotFound)
}
