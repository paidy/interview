package forex.http.rates

import cats.implicits._
import cats.effect.Sync
import cats.data.Validated._
import forex.domain.{ Currency, Price, Timestamp }
import forex.programs.RatesProgram
import forex.programs.rates.{ Protocol => RatesProgramProtocol }
import org.http4s.{ HttpRoutes, Response }
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  private val equalResponse: Currency => F[Response[F]] = curr =>
    Ok(GetApiResponse(curr, curr, Price(1), Timestamp.now))

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      (from, to).tupled.fold(
        err => BadRequest(err.map(_.message).mkString_("\n")),
        validated => {
          val (from, to) = validated
          if (from == to) equalResponse(to)
          else
            rates
              .get(RatesProgramProtocol.GetRatesRequest(from, to))
              .flatMap {
                case Right(rate) => Ok(rate.asGetApiResponse)
                case Left(err)   => UnprocessableEntity(err.msg)
              }
        }
      )
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
