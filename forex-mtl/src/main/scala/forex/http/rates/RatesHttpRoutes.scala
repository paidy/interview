package forex.http
package rates

import cats.effect.Sync
import cats.syntax.flatMap._
import forex.domain.Rate
import forex.programs.RatesProgram
import forex.programs.rates.{ErrorCodes, errors, Protocol => RatesProgramProtocol}
import io.circe.syntax.EncoderOps
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.log4s.{Logger, getLogger}


class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  val logger: Logger = getLogger(getClass)

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      try {
        val response: F[Either[errors.Error, Rate]] = rates.get(RatesProgramProtocol.GetRatesRequest(from, to))
        response.flatMap {
          case Right(_) => response.flatMap(Sync[F].fromEither).flatMap { rate => Ok(rate.asGetApiResponse) }
          case Left(value) => BadRequest(value.asJson)
        }
      } catch {
        case exception: Exception =>
          logger.error(exception)("Exception Occured in Forex Service.")
          val error: errors.Error = errors.Error.RateLookupFailed(ErrorCodes.internalError, "Rate Lookup Failed due to " + exception.getMessage())
          BadRequest(error.asJson)
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
