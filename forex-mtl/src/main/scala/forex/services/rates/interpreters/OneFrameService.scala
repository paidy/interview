package forex.services.rates.interpreters

import java.time.LocalDateTime

import cats.Functor
import forex.cache.OneFrameCache
import forex.domain.Rate
import forex.services.rates.errors.Error.OneFrameLookupFailed
import forex.services.rates.{ errors, Algebra }
import cats.implicits._

import scala.concurrent.duration.FiniteDuration

class OneFrameService[F[_]: Functor](oneFrameCache: OneFrameCache[F], ratesExpiration: FiniteDuration)
    extends Algebra[F] {

  override def get(
      pair: Rate.Pair
  ): F[Either[errors.Error, Rate]] =
    for {
      maybeRate <- oneFrameCache.getRate(pair.generateKey)
    } yield checkValidRate(maybeRate)

  private def checkValidRate(maybeRate: Option[Rate]): Either[errors.Error, Rate] =
    maybeRate match {
      case Some(rate) =>
        if (LocalDateTime.now().getSecond - rate.timestamp.value.getSecond >= ratesExpiration.toSeconds)
          Left(OneFrameLookupFailed("Rate does not exist"))
        else Right(rate)

      case _ => Left(OneFrameLookupFailed("Rate does not exist"))
    }

}

object OneFrameService {
  def apply[F[_]: Functor](oneFrameCache: OneFrameCache[F], ratesExpiration: FiniteDuration): OneFrameService[F] =
    new OneFrameService(oneFrameCache, ratesExpiration)
}
