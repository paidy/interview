package forex.http
package rates

import cats.effect.Sync
import cats.syntax.flatMap._
import forex.programs.RatesProgram
import forex.programs.rates.{ Protocol => RatesProgramProtocol }
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, QueryParams._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(maybeFrom) +& ToQueryParam(maybeTo) =>
      
      val maybeValidatedParams = validateParams(new Params(maybeFrom.toOption, maybeTo.toOption))
        .fold(
          e => Left(Protocol.GetApiError(ErrorType.InvalidRate, e.sanitized)),
          p => Right(p)
        )

      maybeValidatedParams match {
        case Left(e) => BadRequest(e)
        case Right(validatedParams) => {
          val maybeRate = rates.get(RatesProgramProtocol.GetRatesRequest(validatedParams.from, validatedParams.to))

          maybeRate.flatMap { r =>
            r match {
                case Left(_) => 
                  InternalServerError(Protocol.GetApiError(
                    ErrorType.InterpreterError, "Error has occurred. Please try again later."
                  ))
                case Right(rate) => Ok(rate.asGetApiResponse)
            }
          }
        }
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
