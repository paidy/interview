package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.Rate
import forex.services.rates.errors._
import forex.programs.rates.CurrencyConvertor

class OneFrameDummy[F[_]: Applicative] extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    CurrencyConvertor.convertCurrency(pair).asRight[Error].pure[F]

}
