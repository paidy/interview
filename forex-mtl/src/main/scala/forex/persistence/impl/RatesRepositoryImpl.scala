package forex.persistence.impl

import cats.effect.Bracket
import doobie.Transactor
import forex.domain.Currency
import forex.persistence.{ ExceptionHandler, RatesRepository }
import forex.persistence.connection.ConnectionIOSyntax._
import forex.persistence.entity.RateEntity
import forex.persistence.statement.RateStatements
import forex.programs.rates.errors.ForexError

class RatesRepositoryImpl[F[_]](implicit tr: Transactor[F], br: Bracket[F, Throwable], eh: ExceptionHandler)
    extends RatesRepository[F] {

  override def readOneByFromTo(from: Currency, to: Currency): F[ForexError Either Option[RateEntity]] =
    RateStatements
      .readByFromAndTo(from, to)
      .option
      .execute()

  override def updateMany(entities: List[RateEntity]): F[ForexError Either Int] =
    RateStatements.updateMany(entities).execute()
}
