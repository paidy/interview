package forex.programs.rates

import cats.effect.Sync
import cats.implicits.toFunctorOps
import forex.services.RatesService
import forex.services.rates.RateResponse
import forex.programs.rates.errors._

class Program[F[_]: Sync](ratesService: RatesService[F]) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Error Either List[RateResponse]] =
    ratesService.get(request.pairs).map {
      case Right(rates) if rates.isEmpty =>
        Left(Error.RateLookupFailed("No rates found for the given pairs."))
      case Right(rates) =>
        Right(rates)
      case Left(serviceError) =>
        Left(toProgramError(serviceError)) // Convert service error to program error
    }
}

object Program {
  def apply[F[_]: Sync](ratesService: RatesService[F]): Algebra[F] =
    new Program[F](ratesService)
}
