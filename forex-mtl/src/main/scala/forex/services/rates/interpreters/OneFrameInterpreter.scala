package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import forex.domain.Rate
import forex.services.rates.errors._
import forex.services.rates.errors.Error.OneFrameLookupFailed
import forex.thirdPartyApi.oneFrame.OneFrameApiClient
import forex.services.rates.RatesCache
import forex.logging.Logger.logger

class OneFrameInterpreter[F[_]: Applicative](oneFrameApiClient: OneFrameApiClient, ratesCache: RatesCache) extends Algebra[F] {
  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    val cacheResult = ratesCache.get(pair)

    val maybeRate: Either[Error, Rate] = cacheResult match {
      case None => getFromApi(pair)
      case Some(r) => Right(r)
    }

    return Applicative[F].pure(maybeRate)
  }

  private def getFromApi(pair: Rate.Pair): Either[Error, Rate] = {
      val ratesFromClient = oneFrameApiClient.getAll()

      val rateFromMap = ratesFromClient match {
        case Left(_) => Left(OneFrameLookupFailed("Error with getting response from OneFrameApiClient")) 
        case Right(r) => 
          ratesCache.setAll(r.values.toSet)
          Right(r.get(pair))
      }
  
      val rate = rateFromMap match {
        case Left(e) => Left(e)
        case Right(value) => 
          if (value.isEmpty) {
            logger.error(s"Received a valid response from OneFrame API but could not find rate for ${pair}")
            Left(OneFrameLookupFailed("Pair does not exist"))
          } else {
            Right(value.get)
          }
      }

      return rate
  }

}
