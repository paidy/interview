package forex.http.rates

import cats.effect.Async
import forex.programs.RatesProgram
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import forex.model.http.QueryParams._
import forex.model.http.Marshalling._
import forex.model.http.Protocol._


class RatesHttpRoutes[F[_] : Async](rates: RatesProgram[F]) extends Http4sDsl[F] {

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      Ok(rates.get(from, to))
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
