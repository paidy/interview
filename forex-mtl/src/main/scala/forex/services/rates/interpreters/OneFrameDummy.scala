package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.effect.Timer
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.errors._
import fs2.Stream

import scala.concurrent.duration.DurationInt

class OneFrameDummy[F[_]: Applicative] extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[Error].pure[F]

  override def scheduleCacheRefresh()(implicit timer: Timer[F]): Stream[F, Unit] =
    Stream.awakeEvery[F](1.minute).evalMap(_ => Applicative[F].unit)
}
