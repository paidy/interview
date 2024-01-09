package forex.programs.rates

import cats._
import cats.implicits._
import cats.data.EitherT
import errors._
import forex.domain._
import forex.services.{ RatesService, StorageService }

class Program[F[_]: Monad](
    ratesService: RatesService[F],
    storageService: StorageService[F]
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] =
    getOrRequest(Rate.Pair(request.from, request.to))

  private def getOrRequest(pair: Rate.Pair): F[Error Either Rate] =
    storageService
      .get(pair)
      .flatMap {
        case Some(r) => r.asRight[Error].pure[F]
        case None =>
          EitherT(ratesService.getAll)
            .leftMap(toProgramError)
            .flatMap { rates =>
              EitherT.fromOptionF(
                storageService.putAll(rates) *>
                  rates.find(_.pair == pair).pure[F],
                Error.RateLookupFailed(s"Could not find a rate for the pair ${pair.show}"): Error
              )
            }
            .value
      }
}

object Program {

  def apply[F[_]: Monad](
      ratesService: RatesService[F],
      storageService: StorageService[F]
  ): Algebra[F] = new Program[F](ratesService, storageService)

}
