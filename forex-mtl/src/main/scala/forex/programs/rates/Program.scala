package forex.programs.rates

import forex.programs._
import cats.MonadError
import cats.syntax.monadError._
import errors._
import forex.domain.Rate
import forex.services.RatesService

class Program[F[_]: MonadError[?[_], Throwable]](
    ratesService: RatesService[F]
) extends Algebra[F] {

  override def get(request: GetRatesRequest): F[Rate] =
    ratesService.get(Rate.Pair(request.from, request.to)).adaptError {
      case e => RateError.RemoteClientError(e.getMessage)
    }

}

object Program {

  def apply[F[_]: MonadError[?[_], Throwable]](
    ratesService: RatesService[F]
  ): Algebra[F] = new Program[F](ratesService)

}
