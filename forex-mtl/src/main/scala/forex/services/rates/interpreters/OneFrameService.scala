package forex.services.rates.interpreters

import cats.effect.Async
import cats.implicits.{catsSyntaxApplicativeError, toFlatMapOps}
import cats.syntax.functor._
import forex.cache.CurrencyRateCacheAlgebra
import forex.config.OneFrameConfig
import forex.domain.{Currency, Rate}
import forex.services.rates.Algebra
import forex.services.rates.errors.Error.OneFrameLookupFailed
import forex.services.rates.errors._
import forex.services.rates.token.TokenProvider
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.slf4j.LoggerFactory
import sttp.client3._
import sttp.client3.circe._
class OneFrameService[F[_]: Async](config: OneFrameConfig,
                                   backend: SttpBackend[Identity, Any],
                                   cache: CurrencyRateCacheAlgebra[F],
                                   tokenProvider: TokenProvider[F])
    extends Algebra[F] {
  private val logger = LoggerFactory.getLogger(getClass)

  implicit val errorResponseDecoder: Decoder[OneFrameLookupFailed] = deriveDecoder[OneFrameLookupFailed]

  implicit val currencyDecoder: Decoder[Currency] = Currency.decodeCurrency

  implicit val rateDecoder: Decoder[Rate] = Rate.rateDecoder

  implicit val rateOrErrorDecoder: Decoder[Either[OneFrameLookupFailed, List[Rate]]] =
    errorResponseDecoder.either(Decoder.decodeList(rateDecoder))
  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    val cacheKey = s"rates:${pair.from}-${pair.to}"

    type RateIsCached = (Rate, Boolean)
    def getRatesFromOneFrameService(token: String): Either[OneFrameLookupFailed, RateIsCached] = {
      val url = uri"http://${config.http.host}:${config.http.port}/rates?pair=${pair.from}${pair.to}"
      val baseRequest = basicRequest
        .header("token", token)
        .get(uri"$url")
        .readTimeout(config.http.timeout)
        .response(asJson[Either[OneFrameLookupFailed, List[Rate]]])

      backend
        .send(baseRequest)
        .map { response =>
          response.body map {
            case Right(rates) =>
              rates.headOption match {
                case Some(rate) => Right((rate, false))
                case None       => Left(OneFrameLookupFailed("No data"))
              }
            case Left(exception) => Left(exception)
          }
        }
        .getOrElse {
          logger.error("Error getting data from OneFrame")
          Left(OneFrameLookupFailed("No data"))
        }
    }

    def callOneFrameServiceWithToken: F[Either[OneFrameLookupFailed, RateIsCached]] =
      tokenProvider
        .getToken()
        .map {
          case Right(token) => getRatesFromOneFrameService(token)
          case Left(_)      => Left(OneFrameLookupFailed("Internal Service Error"))
        }
        .handleErrorWith { error =>
          logger.error("Error getting token from TokenProvider", error)
          Async[F].pure(Left(OneFrameLookupFailed("Internal Service Error")))
        }

    val rates: F[Either[OneFrameLookupFailed, RateIsCached]] = cache
      .getRates(cacheKey)
      .flatMap {
        case Right(value) => Async[F].pure(Right((value, true)))
        case Left(_) =>
          callOneFrameServiceWithToken
      }

    rates.flatMap {
      case Right((rate, true)) => Async[F].pure(Right(rate))
      case Right((rate, false)) =>
        cache
          .updateRates(cacheKey, rate, config.ratesRefresh.toSeconds.toInt)
          .map(_ => Right(rate)) //TODO: This can fail
      case Left(error) => Async[F].pure(Left(error))
    }
  }
}

object OneFrameService {
  def apply[F[_]: Async](config: OneFrameConfig,
                         backend: SttpBackend[Identity, Any],
                         cache: CurrencyRateCacheAlgebra[F],
                         tokenProvider: TokenProvider[F]): OneFrameService[F] =
    new OneFrameService[F](config, backend, cache, tokenProvider)
}
