package forex.http
package rates

import cats.implicits._
import cats.effect.Sync
import io.circe.syntax._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import forex.programs.RatesProgram
import forex.programs.rates.{Protocol => RatesProgramProtocol}

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      (from, to).mapN(RatesProgramProtocol.GetRatesRequest).fold(
        _ =>
          BadRequest("unable to parse argument 'from' or 'to'".asJson),
        getRatesRequest =>
          if (getRatesRequest.from.equals(getRatesRequest.to)) {
            BadRequest("Parameter 'from' equals to 'to'".asJson)
          } else {
            rates.get(getRatesRequest).flatMap(Sync[F].fromEither).flatMap { rate =>
              Ok(rate.asGetApiResponse)
            }
          }

      )
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
