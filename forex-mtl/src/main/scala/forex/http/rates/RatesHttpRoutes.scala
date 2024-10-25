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
      validateParams(new Params(maybeFrom.toOption, maybeTo.toOption))
      .fold(
        err => Left(BadRequest(Protocol.GetApiError(ErrorType.InvalidRate, err.sanitized))),
        validatedParams => Right(validatedParams)
      )
      .map(validatedParams => rates.get(
        RatesProgramProtocol.GetRatesRequest(validatedParams.from, validatedParams.to)
      ))
      .map(maybeRate => maybeRate.flatMap { r =>
        r.fold(
          _ => InternalServerError(Protocol.GetApiError(
            ErrorType.InterpreterError, "Error has occurred. Please try again later."
            )),
          rate => Ok(rate.asGetApiResponse)
        )
      })
      .merge
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
