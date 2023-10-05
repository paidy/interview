package forex.programs.rates

import cats.Functor
import errors._
import forex.cache.RatesCache
import forex.domain._
import forex.config.ProgramConfig


class Program[F[_] : Functor](
                               config: ProgramConfig, ratesCache: RatesCache[F]
                             ) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] =  ???
//    EitherT(ratesService.get(Rate.Pair(request.from, request.to))).leftMap(toProgramError).value

}

object Program {

  def apply[F[_] : Functor](
                             config: ProgramConfig, ratesCache: RatesCache[F]
                           ): Algebra[F] = new Program[F](config, ratesCache)

}
