package forex.services.rates.interpreters

import forex.services.rates.OneFrameAlgebra
import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{ Price, Rate, Timestamp }
import forex.programs.rates.errors.ForexError

class OneFrameDummy[F[_]: Applicative] extends OneFrameAlgebra[F] {

  override def get(pair: Rate.Pair): F[ForexError Either Rate] =
    Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[ForexError].pure[F]

}
