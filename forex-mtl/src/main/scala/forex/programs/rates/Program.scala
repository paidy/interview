package forex.programs.rates

import cats.Functor
import forex.cache.RatesCache
import forex.model.config.ProgramConfig
import forex.model.domain.Rate
import forex.model.http.Protocol


class Program[F[_] : Functor](
                               config: ProgramConfig, ratesCache: RatesCache[F]
                             ) extends Algebra[F] {

  override def get(request: Protocol.GetApiRequest): F[Rate] =  ???
//    EitherT(ratesService.get(Rate.Pair(request.from, request.to))).leftMap(toProgramError).value

}

object Program {

  def apply[F[_] : Functor](
                             config: ProgramConfig, ratesCache: RatesCache[F]
                           ): Algebra[F] = new Program[F](config, ratesCache)

}
