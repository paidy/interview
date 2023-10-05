package forex.programs.rates

import cats.Functor
import cats.data.EitherT
import errors._
import forex.domain._
import forex.config.ProgramConfig
import forex.services.RatesService

class Program[F[_]: Functor](
    ratesService: RatesService[F], config: ProgramConfig
) extends Algebra[F] {
  val t = config

  override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] =
    EitherT(ratesService.get(Rate.Pair(request.from, request.to))).leftMap(toProgramError).value

}

object Program {

  def apply[F[_]: Functor](
      ratesService: RatesService[F], config: ProgramConfig
  ): Algebra[F] = new Program[F](ratesService, config)

}
