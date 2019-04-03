package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.errors._

class OneForgeDummy[F[_]: Applicative] extends Algebra[F] {

  override def get(pair: Rate.Pair): F[RateError Either Rate] =
    Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[RateError].pure[F]

}
