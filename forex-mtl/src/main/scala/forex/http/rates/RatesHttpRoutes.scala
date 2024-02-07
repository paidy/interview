package forex.http
package rates

import cats.effect.Sync
import cats.implicits._
import forex.domain.{Price, Timestamp}
import forex.programs.RatesProgram
import forex.programs.rates.{Protocol => RatesProgramProtocol}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      (from, to).tupled.fold(
        err => BadRequest(err.toString),
        validated => validated match {
          case (from, to) if from == to => {}
            val sameCurrencyResp = GetApiResponse(from, from, Price(BigDecimal(1)), Timestamp.now)
            Ok(sameCurrencyResp)
          case (from, to) =>
            rates.get(RatesProgramProtocol.GetRatesRequest(from, to)).flatMap {
              case Right(rate) => Ok(rate.asGetApiResponse)
              case Left(err)   => UnprocessableEntity(err.toString)
            }
        }
      )
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
