package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.syntax.applicative._
import forex.domain.{Price, Rate, Timestamp}

class OneForgeDummy[F[_]: Applicative] extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Rate] =
    Rate(pair, Price(BigDecimal(100)), Timestamp.now).pure[F]

}
