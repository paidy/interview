package forex.services.rates.interpreters

import cats.Applicative
import cats.implicits.catsSyntaxApplicativeId
import forex.config.OneFrameConfig
import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate, Timestamp}
import sttp.client3.{HttpURLConnectionBackend, UriContext, basicRequest}
import forex.services.rates.Algebra
import forex.services.rates.errors.Error.OneFrameLookupFailed
import forex.services.rates.errors.{Error => OneFrameError}
import io.circe.{Error => CirceError}

class OneFrameLive[F[_]: Applicative](config: OneFrameConfig) extends Algebra[F] {

  private def getRatesFromOneFrameAPI(token: String, pair: Pair): Either[OneFrameError, String] = {

    val url = uri"http://${config.http.host}:${config.http.port}/rates?pair=${pair.from}${pair.to}"
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

  private def ratesDecoder(jsonString: String): Either[OneFrameError, Rate] = {

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
  override def get(pair: Rate.Pair): F[OneFrameError Either Rate] = {

    getRatesFromOneFrameAPI(config.token, pair) match {
      case Right(jsonString: String) => ratesDecoder(jsonString).pure[F]
      case Left(_) => ???
    }
  }

}