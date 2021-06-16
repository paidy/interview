package forex.client.interpreters

import cats.effect.{ Async, Concurrent, Sync, Timer }
import forex.client.Algebra
import cats.syntax.applicativeError._
import cats.syntax.functor._
import forex.client.errors.{ Error, OneFrameClientResponseError }
import forex.config.OneFrameConfig
import forex.http.rates.Protocol._
import forex.domain.Rate
import forex.domain.Rate.Pair
import forex.util.ForexLogger
import io.circe.{ Error => CError }
import retry._
import sttp.client._
import sttp.client.circe._
import sttp.model.Uri

class OneFrameClient[F[_]: Concurrent: Timer](backend: SttpBackend[Identity, Nothing, NothingT],
                                              oneFrameConfig: OneFrameConfig)
    extends Algebra[F]
    with ForexLogger[F] {
  override implicit protected def sync: Sync[F] = implicitly[Sync[F]]

  private val retryPolicy: RetryPolicy[F] =
    RetryPolicies.limitRetries(oneFrameConfig.retryPolicy.maxRetries) join RetryPolicies.constantDelay(
      oneFrameConfig.retryPolicy.delay
    )

  private def baseRequest: RequestT[Empty, Either[String, String], Nothing] =
    basicRequest.header("token", oneFrameConfig.staticToken).headers(Map("accept" -> "application/json"))

  private def logRetryError(details: RetryDetails): F[Unit] =
    details match {
      case RetryDetails.GivingUp(totalRetries, _) =>
        Logger.error(s"Giving up retrying to get data from OneFrame, after $totalRetries retries")
      case RetryDetails.WillDelayAndRetry(nextDelay, _, _) =>
        Logger.warn(s"Error getting data from OneFrame, will retry in ${nextDelay.toSeconds} seconds")
    }

  override def getRates(pairs: Vector[Pair]): F[Either[Error, List[Rate]]] = {
    val params: Seq[(String, String)] = pairs.map((pair: Rate.Pair) => "pair" -> s"${pair.from}${pair.to}")
    val url: Uri                      = uri"http://${oneFrameConfig.httpClient.host}:${oneFrameConfig.httpClient.port}/rates?$params"

    val request: F[Either[Error, List[Rate]]] = Async[F]
      .delay {
        backend
          .send {
            baseRequest
              .readTimeout(oneFrameConfig.httpClient.timeout)
              .get(url)
              .response(asJson[Either[OneFrameClientResponseError, List[Rate]]])
          }
      }
      .map[Either[Error, List[Rate]]] {
        response: Response[Either[ResponseError[CError], Either[OneFrameClientResponseError, List[Rate]]]] =>
          response.body match {
            case Left(error: ResponseError[CError])              => Left(OneFrameClientResponseError(s"Circe failure! ${error}"))
            case Right(Left(error: OneFrameClientResponseError)) => Left(OneFrameClientResponseError(error.error))
            case Right(Right(rates: List[Rate]))                 => Right(rates)
          }
      }
      .handleError {
        case t: Throwable => Left(OneFrameClientResponseError(t.getMessage))
      }

    retryingM[Either[Error, List[Rate]]](retryPolicy, _.isRight, (_, details) => logRetryError(details))(request)

  }
}

object OneFrameClient {
  def apply[F[_]: Concurrent: Timer](backend: SttpBackend[Identity, Nothing, NothingT],
                                     oneFrameConfig: OneFrameConfig): OneFrameClient[F] =
    new OneFrameClient(backend, oneFrameConfig)
}
