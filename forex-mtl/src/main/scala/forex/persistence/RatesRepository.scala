package forex.persistence

import cats.effect.Bracket
import doobie.Transactor
import forex.domain.Currency
import forex.persistence.entity.RateEntity
import forex.persistence.impl.RatesRepositoryImpl
import forex.programs.rates.errors.ForexError

trait RatesRepository[F[_]] {

  def readOneByFromTo(from: Currency, to: Currency): F[ForexError Either Option[RateEntity]]

  def updateMany(entities: List[RateEntity]): F[ForexError Either Int]

}

object RatesRepository {

  def apply[F[_]]()(implicit tr: Transactor[F], br: Bracket[F, Throwable], eh: ExceptionHandler): RatesRepository[F] =
    new RatesRepositoryImpl[F]

}
