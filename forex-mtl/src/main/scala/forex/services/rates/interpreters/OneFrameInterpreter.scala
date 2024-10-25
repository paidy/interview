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
      val ratesFromApi = oneFrameApiClient.getAll()

      val rateFromApi = ratesFromApi.fold(
        _ => Left(OneFrameLookupFailed("Error with getting response from OneFrameApiClient")),
        r => {
          ratesCache.setAll(r.values.toSet)
          Right(r.get(pair))
        }
      )
  
      rateFromApi.fold(
        e => Left(e),
        maybeRate => 
          if (maybeRate.isEmpty) {
            logger.error(s"Received a valid response from OneFrame API but could not find rate for ${pair}")
            Left(OneFrameLookupFailed("Pair does not exist"))
          } else {
            Right(maybeRate.get)  
          }
      )
  }

}
