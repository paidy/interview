package forex.services.rates.token

import cats.effect.Async
import cats.implicits.{toFlatMapOps, toFunctorOps}
import forex.http.ratelimitter.error.InvalidToken

class TokenProvider[F[_]: Async](cache: F[TokenCacheAlgebra[F]]) {

  def getToken(): F[InvalidToken Either String] = cache.flatMap { cache =>
    cache.getToken().flatMap {
      case Some(token) => cache.incrementUsage(token).map(_ => Right(token))
      case None        => Async[F].pure(Left(InvalidToken("No tokens available")))
    }
  }
}
