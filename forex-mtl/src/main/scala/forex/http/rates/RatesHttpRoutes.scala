package forex.http.rates

import cats.effect.{Async, Sync}
import cats.syntax.flatMap._
import forex.model.domain.Rate
import forex.model.http.Protocol
import forex.programs.RatesProgram
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_] : Async](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import forex.model.http.Converters._, forex.model.http.QueryParams._, forex.model.http.Protocol._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      rates.get(from, to)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
