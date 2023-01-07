package forex.programs.rates

import cats._
import cats.implicits._
import cats.data.{EitherT, NonEmptyList}
import errors._
import forex.domain.Rate.Pair
import forex.domain._
import forex.services._
import forex.services.rates.errors.{Error => RatesServiceError}
import org.typelevel.log4cats.Logger


class Program[F[_] : Monad : Logger](
  ratesService: RatesService[F],
  cacheService: CacheService[F]
) extends Algebra[F] {

    override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] = {
      val allPairs: NonEmptyList[Pair] = NonEmptyList.fromListUnsafe(
        (for {
          aCurrency <- Currency.values
          bCurrency <- Currency.values if aCurrency != bCurrency
        } yield Pair(aCurrency, bCurrency)).toList
      )
      val targetPair = Pair(request.from, request.to)
      for {
        cacheResult <- cacheService.get(targetPair)
        result <- cacheResult match {
          case None =>
            for {
              _ <- Logger[F].info(s"No Cache hit for ${targetPair.show}, refresh cache")
              eitherPairRates <- ratesService.getMany(allPairs).map{ eitherRates =>
                eitherRates.map(rates => rates.map(rate => (rate.pair, rate)).toList.toMap)
              }
              _ <- eitherPairRates match {
                case Left(err) => Logger[F].error(s"Get rate from external failed: $err")
                case Right(pairRates) => cacheService.setMany(pairRates) *> Logger[F].info(s"Refreshed cache!")
              }
            } yield eitherPairRates.map(m => m(targetPair))
          case Some(rate) =>
            for {
              _ <- Logger[F].info(s"Cache hit for ${targetPair.show}")
              result <- EitherT.pure[F, RatesServiceError](rate).value
            } yield result
        }
      } yield result.leftMap(toProgramError)
    }

}

object Program {

  def apply[F[_]: Monad: Logger](
    ratesService: RatesService[F],
    cacheService: CacheService[F]
  ): Algebra[F] = new Program[F](ratesService, cacheService)

}
