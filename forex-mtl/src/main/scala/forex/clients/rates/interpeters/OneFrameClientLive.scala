package forex.clients.rates.interpeters

import cats.data.EitherT
import cats.effect.Async
import cats.implicits.{ catsSyntaxApplicativeError, catsSyntaxEitherId, toBifunctorOps }
import forex.clients.rates.OneFrameClientAlgebra
import forex.clients.rates.protocol.OneFrameResponse.{ ErrorResponse, SuccessResponse }
import forex.clients.rates.protocol.{ OneFrameRate, OneFrameResponse }
import forex.config.ApplicationConfig.OneFrameClientConfig
import forex.domain.Currency
import forex.programs.rates.errors.ForexError
import forex.programs.rates.errors.ForexError.{ ExternalServiceError, InternalError, RateLookupFailed }
import io.circe.{ parser, Decoder }
import sttp.client3._
import sttp.model.{ Header, StatusCode }

class OneFrameClientLive[F[_]: Async](config: OneFrameClientConfig, backend: SttpBackend[F, Any])
    extends OneFrameClientAlgebra[F] {

  def getRates: F[ForexError Either List[OneFrameRate]] =
    handleResponse[OneFrameResponse] {
      basicRequest
        .get(uri"${config.host}/rates?".addParams(Currency.permutationsPairsString: _*))
        .headers(Header("token", config.token))
        .send(backend)
    }.value

  private def handleResponse[S <: OneFrameResponse: Decoder](
      f: F[Response[Either[String, String]]]
  ): EitherT[F, ForexError, List[OneFrameRate]] =
    f.attemptT
      .leftMap(ex => ExternalServiceError(ex.getMessage))
      .flatMap { response =>
        response.body.fold(errorHandler(response.code, _), successHandler[S])
      }

  private def successHandler[S <: OneFrameResponse: Decoder](
      rawJson: String
  ): EitherT[F, ForexError, List[OneFrameRate]] =
    EitherT.fromEither[F] {
      parser
        .parse(rawJson)
        .leftMap(parseError => InternalError(parseError.message, "Internal error, please contact support team"))
        .flatMap(
          _.as[S]
            .leftMap(decodeError => InternalError(decodeError.message, "Internal error, please contact support team"))
            .flatMap(handleOneFrameResponse)
        )
    }

  private def handleOneFrameResponse(resp: OneFrameResponse): Either[ForexError, List[OneFrameRate]] =
    resp match {
      case SuccessResponse(rates) => rates.asRight[ForexError]
      case ErrorResponse(error)   => ExternalServiceError(error).asLeft[List[OneFrameRate]]
    }

  private def errorHandler(statusCode: StatusCode, rawError: String): EitherT[F, ForexError, List[OneFrameRate]] =
    EitherT.fromEither(RateLookupFailed(rawError, rawError, statusCode).asLeft[List[OneFrameRate]])

}
