package forex.services.rates.interpreters

import cats.Monad
import cats.data.EitherT
import cats.implicits._
import forex.config.ProviderConfig
import forex.domain._
import forex.services.rates
import forex.services.rates.errors
import forex.services.rates.interpreters.Implicits._
import io.circe.Decoder
import sttp.client3._
import sttp.client3.circe._
import sttp.model.Uri

class OneFrameLive[F[_]: Monad](config: ProviderConfig, backend: SttpBackend[F, _]) extends rates.Algebra[F] {
  import Protocol._

  private val tokenHeader = "token"

  private val pairs = for {
    from <- Currency.values
    to <- Currency.values
  } yield Rate.Pair(from, to)

  private val baseUri = uri"${config.uri}/rates"

  private val getAllUri = baseUri.withParams(pairs.map(p => ("pair", p.show)): _*)

  override def get(pair: Rate.Pair): F[Either[errors.Error, Rate]] =
    (for {
      rates <- EitherT(sendRequest[List[OneFrameResponse]](baseUri.withParam("pair", pair.show)))
      responseRate <- EitherT.fromOption[F](
                       rates.headOption,
                       errors.Error.OneFrameLookupFailed(s"Failed to get rate for ${pair.show}"): errors.Error
                     )
      rate <- EitherT(responseRate.toRate[F])
    } yield rate).value

  override def getAll: F[Either[errors.Error, List[Rate]]] =
    (for {
      response <- EitherT(sendRequest[List[OneFrameResponse]](getAllUri))
      parsedRates <- EitherT(response.toRates[F])
    } yield parsedRates).value

  private def sendRequest[T: Decoder](uri: Uri): F[Either[errors.Error, T]] =
    basicRequest
      .header(tokenHeader, config.token)
      .get(uri)
      .response(asJson[T])
      .send(backend)
      .map { response =>
        response.body.leftMap(e => errors.Error.ApiRequestFailed(e.getMessage))
      }
}
