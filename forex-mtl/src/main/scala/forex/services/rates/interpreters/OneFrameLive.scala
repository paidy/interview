package forex.services.rates.interpreters

import cats.Monad
import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import forex.domain.Rate
import forex.persistence.RatesRepository
import forex.programs.rates.errors.ForexError
import forex.programs.rates.errors.ForexError.RateLookupFailed
import forex.services.rates.OneFrameAlgebra
import sttp.model.StatusCode

class OneFrameLive[F[_]: Monad](repo: RatesRepository[F]) extends OneFrameAlgebra[F] {

  override def get(pair: Rate.Pair): F[ForexError Either Rate] =
    EitherT(repo.readOneByFromTo(pair.from, pair.to)).flatMap { entityOpt =>
      val entityEither = entityOpt match {
        case Some(entity) => Rate.fromRateEntity(entity).asRight[ForexError]
        case None =>
          RateLookupFailed("Currency pair not found", "Currency pair not found", StatusCode.NotFound).asLeft[Rate]
      }
      EitherT.fromEither(entityEither)
    }.value

}
