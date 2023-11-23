package forex.http
package rates

import cats.effect.Sync
import cats.syntax.all._
import forex.programs.RatesProgram
import forex.domain.Rate.Pair
import forex.programs.rates.Protocol
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._
  import QueryParams._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      Sync[F].delay(println(s"Request received for $from to $to")) *>
        rates.get(Protocol.GetRatesRequest(List(Pair(from, to)))).flatMap(Sync[F].fromEither).flatMap { rates =>
        Ok(rates.map(_.asGetApiResponse))
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
