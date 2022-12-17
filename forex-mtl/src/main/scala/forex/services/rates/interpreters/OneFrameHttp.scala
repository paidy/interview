package forex.services.rates.interpreters

import cats._
import org.http4s.client._
import forex.domain.Rate
import forex.services.rates.{Algebra, errors}

class OneFrameHttp[F[_]: Applicative](
  client: Client[F]
) extends Algebra[F]{
  override def get(pair: Rate.Pair): F[Either[errors.Error, Rate]] = ???
}
