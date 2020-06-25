package forex.programs.rates

import cats.Applicative
import cats.data.EitherT
import cats.syntax.either._
import forex.domain._
import errors._
import forex.services.RatesService

class Program[F[_]: Applicative](
    ratesService: RatesService[F]
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] =
    request match {
      case Protocol.GetRatesRequest(Some(from), Some(to)) if from != to =>
        EitherT(ratesService.get(Rate.Pair(from, to))).leftMap(toProgramError(_)).value
      case _ => toInvalidInputError("invalid input currency pair").asLeft[Rate].toEitherT.value
    }
}

object Program {

  def apply[F[_]: Applicative](
      ratesService: RatesService[F]
  ): Algebra[F] = new Program[F](ratesService)

}
