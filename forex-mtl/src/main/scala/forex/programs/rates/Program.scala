package forex.programs.rates

import cats.Functor
import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import errors._
import forex.domain._
import forex.services.{CacheService, RatesService}

import scala.annotation.nowarn

class Program[F[_]: Functor](
    ratesService: RatesService[F],
    cacheService: CacheService
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] =
    EitherT(ratesService.get(Rate.Pair(request.from, request.to))).leftMap(toProgramError(_)).value

  @nowarn
  private def getFromCacheOrHitOneFrame(pair: Rate.Pair): Any =
    cacheService.get(pair) match {
      case Some(rate) => rate.asRight
      case None => ratesService.get(pair)
    }
}

object Program {

  def apply[F[_]: Functor](
      ratesService: RatesService[F],
      cacheService: CacheService
  ): Algebra[F] = new Program[F](ratesService, cacheService)

}
