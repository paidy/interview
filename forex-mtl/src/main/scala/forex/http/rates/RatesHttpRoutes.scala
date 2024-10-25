package forex.http
package rates

import cats.effect.Sync
import cats.implicits.catsSyntaxApplicativeError
import cats.syntax.flatMap._
import forex.programs.RatesProgram
import forex.programs.rates.errors.Error.{RateInvalidString, RateLookupFailed}
import forex.programs.rates.{Protocol => RatesProgramProtocol}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      (from, to) match {
        case (Right(from), Right(to)) =>
          rates.get(RatesProgramProtocol.GetRatesRequest(from, to))
            .flatMap(Sync[F].fromEither)
            .flatMap { rate =>
              Ok(rate.asGetApiResponse)
            }
            .handleErrorWith {
              case e: RateLookupFailed =>
                // Handle your specific error here and provide a custom response
                InternalServerError(s"Error: ${e.msg}")

              case e: RateInvalidString =>
                // Handle other types of errors and provide a generic error response
                InternalServerError(s"Internal Server Error: ${e.msg}")
            }

        case (Left(error), _) =>
          BadRequest(error.msg)

        case (_, Left(error)) =>
          BadRequest(error.msg)
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
