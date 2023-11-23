package forex.services.rates.interpreters

import cats.effect._
import cats.implicits._
import forex.domain.Rate
import forex.services.rates.{Algebra, RateResponse}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s._
import org.http4s.circe._
import io.circe.Json
import forex.services.rates.errors._

class OneFrameInterpreter[F[_]: ConcurrentEffect](oneFrameUri: Uri, apiToken: String) extends Algebra[F] {
  private val F = ConcurrentEffect[F]
  override def get(pairs: List[Rate.Pair]): F[Error Either List[RateResponse]] = {
    val baseUri = Uri.fromString(oneFrameUri.toString()).getOrElse(throw new Exception("Invalid OneFrame API URI"))
    val uri = baseUri.withPath("/rates")
    val uriWithParams = pairs.foldLeft(uri)((currentUri, pair) => currentUri.withQueryParam("pair", s"${pair.from}${pair.to}"))

    val request = Request[F](
      method = Method.GET,
      uri = uriWithParams,
      headers = Headers.of(Header("token", apiToken))
    )

    BlazeClientBuilder[F](scala.concurrent.ExecutionContext.global).resource.use { client =>
      client.run(request).use { response =>
        response.status match {
          case Status.Ok =>
            response.as[Json].flatMap { json =>
              json.as[List[RateResponse]] match {
                case Right(rates) => F.pure(Right(rates))
                case Left(e) => F.pure(Left(Error.OneFrameLookupFailed(e.getMessage)))
              }
            }
          case _ =>
            F.pure(Left(Error.OneFrameLookupFailed("Unexpected response status")))
        }
      }
    }
  }
}
