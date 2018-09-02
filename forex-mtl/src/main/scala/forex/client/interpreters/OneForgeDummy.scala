package forex.client.interpreters

import cats.Applicative
import cats.syntax.applicative._
import forex.domain.{Price, Rate, Timestamp}
import forex.client.algebra.OneForgeClient

class OneForgeDummy[F[_]: Applicative] extends OneForgeClient[F] {

  override def get(pair: Rate.Pair): F[Rate] =
    Rate(pair, Price(BigDecimal(100)), Timestamp.now).pure[F]

}
