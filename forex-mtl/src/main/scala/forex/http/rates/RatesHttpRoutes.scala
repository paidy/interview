package forex.http
package rates

import cats.data.NonEmptyList
import cats.effect.Sync
import forex.programs.RatesProgram
import forex.programs.rates.{ Protocol => RatesProgramProtocol }
import forex.programs.rates.errors.{ Error => ProgramError }
import org.http4s.{ HttpRoutes, ParseFailure }
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import cats.implicits._

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(validatedFrom) +& ToQueryParam(validatedTo) =>
      (
        validatedFrom,
        validatedTo
      ).mapN { (from, to) =>
          rates
            .get(RatesProgramProtocol.GetRatesRequest(from, to))
            .flatMap(Sync[F].fromEither)
            .flatMap { rate =>
              Ok(rate.asGetApiResponse)
            }
            .recoverWith {
              case pe: ProgramError => InternalServerError(pe.asApiError)
              case ex               => InternalServerError(s"Internal server error: unknown error ${ex.getMessage}")
            }
        }
        // right param names with bad param values
        .leftMap(
          (nel: NonEmptyList[ParseFailure]) =>
            BadRequest(s"Invalid request params: ${nel.map(_.getMessage()).toList.mkString(",")}")
        )
        .merge
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
