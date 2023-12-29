package forex.client

import cats.effect.Async
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import forex.config.OneFrameConfig
import forex.domain.OneFrameCurrencyInformation
import forex.domain.Rate.Pair
import forex.services.rates.errors.Error.OneFrameLookupFailed
import forex.services.rates.errors._
import sttp.client._
import sttp.client.circe.asJson

trait OneFrameClient[F[_]] {
  def getRates(pairs: Vector[Pair]): F[Either[Error, List[OneFrameCurrencyInformation]]]
}

class OneFrameHttpClient[F[_]: Async](
    oneFrameConfig: OneFrameConfig,
    implicit val backend: SttpBackend[Identity, Nothing, NothingT]
) extends OneFrameClient[F]
    with LazyLogging {

  def getRates(pairs: Vector[Pair]): F[Either[Error, List[OneFrameCurrencyInformation]]] = {
    val param = pairs.map((pair: Pair) => "pair" -> s"${pair.from}${pair.to}")
    val url   = uri"http://${oneFrameConfig.url}/rates?$param"

    val request = basicRequest
      .get(uri = url)
      .contentType("application/json")
      .header("token", oneFrameConfig.token)
      .response(asJson[List[OneFrameCurrencyInformation]])
      .send()
      .pure[F]
    request.map { request =>
      request.body match {
        case Right(rates) => rates.asRight[Error]
        case Left(error) =>
          OneFrameLookupFailed(s" Parsing error - ${error.getMessage}").asLeft[List[OneFrameCurrencyInformation]]
      }
    }
  }.recoverWith {
    case t =>
      logger.error(s"Failed to get rates with error: ${t.getMessage}")
      Async[F].pure(
        OneFrameLookupFailed(s"Failed to get rates with error: ${t.getMessage}")
          .asLeft[List[OneFrameCurrencyInformation]]
      )
  }
}

object OneFrameClient {
  def apply[F[_]: OneFrameClient]: OneFrameClient[F] = implicitly

  implicit def OneFrameHttpClient[F[_]: Async](config: OneFrameConfig)(
      implicit backend: SttpBackend[Identity, Nothing, NothingT]
  ): OneFrameClient[F] =
    new OneFrameHttpClient[F](config, backend)
}
