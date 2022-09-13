package forex.services.rates.interpreters

import cats.effect.Concurrent
import cats.implicits.toFunctorOps
import forex.domain.Rate
import forex.services.rates.errors.Error
import forex.services.rates.{Algebra, BatchedAlgebra}

class OneFrameLive[F[_]: Concurrent](
  private val batchService: BatchedAlgebra[F]
) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    batchService.get(Seq(pair)).map(_.map(_.head))
  }

}

