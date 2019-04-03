package forex.programs.rates

import cats.MonadError
import cats.syntax.monadError._
import errors._
import forex.domain._
import forex.services.RatesService

class Program[F[_]: MonadError[?[_], Throwable]](
    ratesService: RatesService[F]
) extends Algebra[F] {

  override def get(request: Program.GetRatesRequest): F[Rate] =
    ratesService.get(Rate.Pair(request.from, request.to)).adaptError {
      case e => RateError.RemoteClientError(e.getMessage)
    }

}

object Program {

  def apply[F[_]: MonadError[?[_], Throwable]](
    ratesService: RatesService[F]
  ): Algebra[F] = new Program[F](ratesService)

  final case class GetRatesRequest(
      from: Currency,
      to: Currency
  )
}
