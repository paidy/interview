package forex.http
package rates

import cats._
import cats.effect.IO
import cats.implicits.toShow
import org.http4s._
import org.http4s.implicits._
import org.http4s.dsl.io._
import forex.Generators._
import forex.domain.Rate
import forex.programs.RatesProgram
import forex.programs.rates.errors.Error.RateLookupFailed
import forex.programs.rates.Protocol.GetRatesRequest
import io.circe.syntax.EncoderOps

object RatesHttpRoutesSuite extends HttpSuite {

  import Protocol._
  import Converters._

  implicit val showRate: Show[Rate] = Show.show(rate => rate.asJson.noSpaces)
  def successRatesProgram(rate: Rate): RatesProgram[IO] =
    (_: GetRatesRequest) => IO.pure(Right(rate))

  def failedRatesProgram(errMsg: String): RatesProgram[IO] =
    (_: GetRatesRequest) => IO.pure(Left(RateLookupFailed(errMsg)))

  test("Request with validated Currency") {
    forall(rateGen) { rate =>
      val req: Request[IO] = Request[IO](
        GET,
        uri"/rates".withQueryParams(
          Map[String, String](
            "from" -> rate.pair.from.show,
            "to" -> rate.pair.to.show
          )
        )
      )
      val routes = new RatesHttpRoutes[IO](successRatesProgram(rate)).routes
      if (rate.pair.from == rate.pair.to) {
        expectHttpStatus(routes, req)(Status.BadRequest)
      } else {
        expectHttpBodyAndStatus(routes, req)(rate.asGetApiResponse, Status.Ok)
      }
    }
  }
}
