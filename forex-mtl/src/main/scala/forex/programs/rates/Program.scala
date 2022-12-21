package forex.programs.rates

import cats._
import cats.implicits._
import cats.data.EitherT
import errors._
import forex.domain._
import forex.services._
import forex.services.rates.errors.{Error => RatesServiceError}
import org.typelevel.log4cats.Logger


class Program[F[_]: Monad : Logger](
  ratesService: RatesService[F],
  cacheService: CacheService[F]
) extends Algebra[F] {

    override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] = {
      val pair = Rate.Pair(request.from, request.to)
      for {

        cacheResult <- cacheService.get(pair)
        result <- cacheResult match {
          case None =>
            for {
              _ <- Logger[F].info(s"No Cache hit for ${pair.show}")
              result <- ratesService.get(pair)
              _ <- result match {
                case Left(err) => Logger[F].error(s"Get rate from external failed: ${err}")
                case Right(rate) =>
                  cacheService.set(pair, rate) *> Logger[F].info(s"Set cache with key ${pair}")
              }
            } yield result
          case Some(rate) =>
            for {
              _ <- Logger[F].info(s"Cache hit for ${pair.show}")
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
