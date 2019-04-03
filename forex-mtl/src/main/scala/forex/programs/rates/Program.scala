package forex.programs.rates

import cats.Functor
import cats.data.EitherT
import errors._
import forex.domain._
import forex.services.RatesService

class Program[F[_]: Functor](
    ratesService: RatesService[F]
) extends Algebra[F] {

  override def get(request: Program.GetRatesRequest): F[RateError Either Rate] =
    EitherT(ratesService.get(Rate.Pair(request.from, request.to))).leftMap(toProgramError(_)).value

}

object Program {

  def apply[F[_]: Functor](
    ratesService: RatesService[F]
  ): Algebra[F] = new Program[F](ratesService)

  final case class GetRatesRequest(
      from: Currency,
      to: Currency
  )
}
