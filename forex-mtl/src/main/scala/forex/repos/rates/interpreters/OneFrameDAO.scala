package forex.repos.rates.interpreters

import cats.effect.{ Resource, Sync }
import forex.config.OneFrameConfig
import forex.domain.{ Currency, Rate }
import forex.repos.rates.interpreters.OneFrameProtocol.OneFrameResponse
import forex.repos.rates.{ errors, Algebra }
import org.http4s.{ Header, Headers, Method, Request, Uri }
import org.http4s.client.Client
import forex.http._
import cats.implicits._
import forex.repos.rates.interpreters.OneFrameConverter._

class OneFrameDAO[F[_]: Sync](httpClient: Resource[F, Client[F]], settings: OneFrameConfig) extends Algebra[F] {
  override def getAllRates: F[Either[errors.Error, Seq[Rate]]] = {
    val allRates =
      Currency.values
        .combinations(2)
        .toList
        .flatMap {
          case c1 :: c2 :: Nil =>
            List(s"$c1$c2", s"$c2$c1")
        }

    httpClient.use { client =>
      val uriEither =
        for {
          uri <- Uri.fromString(s"http://${settings.http.host}:${settings.http.port}/rates")
          paramedUri <- uri.withMultiValueQueryParams(Map("pair" -> allRates)).asRight
        } yield paramedUri

      uriEither match {
        case Left(parseFailure) =>
          Sync[F].pure(errors.Error.InvalidAddress(parseFailure.getMessage()).asLeft)
        case Right(uri) =>
          val headers = Headers.of(Header("token", settings.token))
          val request =
            Request[F](
              method = Method.GET,
              uri = uri,
              headers = headers
            )

          client
            .expect[OneFrameResponse](request)
            .attempt
            .map(_.bimap(ex => errors.Error.OneFrameQueryFailed(ex.getMessage), resp => resp.toRates))
      }
    }
  }
}
