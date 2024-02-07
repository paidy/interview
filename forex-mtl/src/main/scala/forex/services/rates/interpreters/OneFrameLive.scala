package forex.services.rates.interpreters

import cats.Applicative
import cats.implicits._
import forex.config.{OneFrameConfig, RedisConfig}
import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.repo.{RedisCache}
import sttp.client3.{HttpURLConnectionBackend, UriContext, basicRequest}
import forex.services.rates.{Algebra, OneFrameResp}
import forex.services.rates.errors.RateServiceError.OneFrameLookupFailed
import forex.services.rates.errors.RateServiceError
import io.circe.{Error => CirceError}

class OneFrameLive[F[_]: Applicative](oneFrameConfig: OneFrameConfig, redisConfig: RedisConfig) extends Algebra[F] {

  private def getRatesFromOneFrameAPI(token: String): Either[RateServiceError, String] = {

    val allPairs = Currency.allPairs

    val baseUrl = uri"http://${oneFrameConfig.http.host}:${oneFrameConfig.http.port}/rates"
    val url = baseUrl.withParams(allPairs.map(p => ("pair", p.show)): _*)

    val request = basicRequest
      .header("token", token)
      .get(url)

    val backend = HttpURLConnectionBackend()
    val response = request.send(backend)

    response.body match {
      case Right(jsonString) => Right(jsonString)
      case Left(errorMessage) => Left(OneFrameLookupFailed(errorMessage))
    }
  }

  private def ratesDecoder(jsonString: String): Either[RateServiceError, Rate] = {

    val result: Either[CirceError, List[OneFrameResp]] = io.circe.parser.decode[List[OneFrameResp]](jsonString)

    val convertOneFrameRateIntoRate: (OneFrameResp) => Rate = oneFrameResp => Rate(
        Pair(Currency.fromString(oneFrameResp.from), Currency.fromString(oneFrameResp.to)),
        Price(oneFrameResp.price),
        Timestamp.fromString(oneFrameResp.timestamp)
      )

    result match {
      case Right(resp) => Right(resp.map(x => convertOneFrameRateIntoRate(x)).head)
      case Left(error) => Left(OneFrameLookupFailed(error.getMessage))
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
    val tmp: Option[Rate] = rc.get(pair)

    tmp match {
      case Some(rate: Rate) => rate.asRight[RateServiceError].pure[F]
      case None => getRatesFromOneFrameAPI(oneFrameConfig.token) match {
        case Right(jsonString: String) => ratesDecoder(jsonString).pure[F]
        case Left(error) => error.asLeft[Rate].pure[F]
      }
    }
  }

}