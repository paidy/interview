package forex.services.rates.interpreters

import cats.Applicative
import cats.implicits._
import forex.config.{OneFrameConfig, RedisConfig}
import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.repo.RedisCache
import sttp.client3.{HttpURLConnectionBackend, UriContext, basicRequest}
import forex.services.rates.{Algebra, OneFrameResp}
import forex.services.rates.errors.RateServiceError.{OneFrameAPIRequestFailed, OneFrameParseRatesFailed}
import forex.services.rates.errors.RateServiceError
import io.circe.{Error => CirceError}
import sttp.model.Uri

import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}


class OneFrameLive[F[_]: Applicative](oneFrameConfig: OneFrameConfig, redisConfig: RedisConfig) extends Algebra[F] {

  private def sendRequest(url: Uri, token: String): Either[RateServiceError, String]  = {
    basicRequest
      .header("token", token)
      .get(url)
      .send(HttpURLConnectionBackend())
      .body match {
      case Right(jsonString) => Right(jsonString)
      case Left(errorMessage) => Left(OneFrameAPIRequestFailed(errorMessage))
    }
  }

  private def getRatesFromOneFrameAPI(token: String): Either[RateServiceError, String] = {

    val oneFrameHostPort = s"${oneFrameConfig.http.host}:${oneFrameConfig.http.port}"
    val baseUrl = uri"http://${oneFrameHostPort}/rates"

    val allPairs: List[String] = Currency.allPairs.map(p => s"${p._1}${p._2}") // [USDJPY, JPYSGD, ...]
    val url = baseUrl.withParams(allPairs.map(p => ("pair", p)): _*)

    sendRequest(url, token)
  }

  private def ratesDecoder(jsonString: String): Either[RateServiceError, List[Rate]] = {

    val result: Either[CirceError, List[OneFrameResp]] = io.circe.parser.decode[List[OneFrameResp]](jsonString)

    val convertOneFrameRateIntoRate: (OneFrameResp) => Rate = oneFrameResp => Rate(
        Pair(Currency.fromString(oneFrameResp.from), Currency.fromString(oneFrameResp.to)),
        Price(oneFrameResp.price),
        Timestamp.fromString(oneFrameResp.timestamp)
      )

    result match {
      case Right(resp) => Right(resp.map(x => convertOneFrameRateIntoRate(x)))
      case Left(error) => Left(OneFrameParseRatesFailed(error.getMessage))
    }
  }

//  Learned: both of this are the same
//  F[OneFrameError Either Rate]
//  F[Either[OneFrameError, Rate]]
  override def get(pair: Rate.Pair): F[RateServiceError Either Rate] = {

//    Step 1: Check if the pair already cached
//    Step 2a: If yes,
//       - return answer: Rate
//    Step 2b: If no,
//       - request from the One Frame Service: allPairs
//       -

    val rc = RedisCache.getInstance(redisConfig)
    val maybeRate: Option[Rate] = rc.get(pair)

    maybeRate match {
      case Some(rate: Rate) => rate.asRight[RateServiceError].pure[F]
      case None => getRatesFromOneFrameAPI(oneFrameConfig.token) match {
        case Right(jsonString: String) => ratesDecoder(jsonString) match {
          case Right(rates: List[Rate]) => {
            val setAllPairsFuture = rc.setAll(rates)
            setAllPairsFuture.onComplete {
              case Success(_) => println(s"success setAll pairs in Redis")
              case Failure(e) => println(s"failed to setAll pairs in Redis, error: ${e}")
            }
            rates.filter(r => r.pair == pair).head.asRight[RateServiceError].pure[F]
          }
          case Left(error) => error.asLeft[Rate].pure[F]
        }
        case Left(error) => error.asLeft[Rate].pure[F]
      }
    }
  }

}