package forex.http
package rates

import cats.data.Validated.Valid
import cats.effect.Sync
import cats.syntax.flatMap._
import forex.programs.RatesProgram
import forex.programs.rates.{ Protocol => RatesProgramProtocol }
import org.http4s.{ HttpRoutes, Response, Status }
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      (from, to) match {
        case (Valid(from), Valid(to)) =>
          rates
            .get(RatesProgramProtocol.GetRatesRequest(from, to))
            .flatMap {
              case Left(err) =>
                Sync[F].pure(Response[F]().withStatus(Status.InternalServerError).withEntity(err.getMessage))
              case Right(rate) => Ok(rate.asGetApiResponse)
            }
        case (_, _) => BadRequest("Currency not supported.")
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
