package forex.programs.rates

import cats.Applicative
import cats.data.EitherT
import cats.implicits._
import errors._
import forex.cache.rates.RatesCache
import forex.domain._
import forex.services.RatesService

class Program[F[_]: Applicative](
    ratesService: RatesService[F],
    ratesCache: RatesCache
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] = {
    ratesCache
      .getRate(request.from.show, request.to.show)
      .fold(
        EitherT(
          ratesService.get(
            Rate.Pair(request.from, request.to)
          )
        ).map(rate => {
            ratesCache.setRate(request.from.show, request.to.show, rate)
            rate
          })
          .leftMap(toProgramError).value
      )(
        rate => Applicative[F].pure(rate.asRight[Error])
      )
  }
}

object Program {

  def apply[F[_]: Applicative](
      ratesService: RatesService[F],
      ratesCache: RatesCache
  ): Algebra[F] = new Program[F](ratesService, ratesCache)

}
