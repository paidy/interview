package forex.rates
package program

import algebra.Rates
import cats.MonadError
import cats.syntax.monadError._
import errors._
import forex.domain.Rate
import forex.client.algebra.OneForgeClient

class RatesProgram[F[_]](oneForge: OneForgeClient[F])(implicit M: MonadError[F, Throwable]) extends Rates[F] {

  override def get(request: GetRatesRequest): F[Rate] =
    oneForge.get(Rate.Pair(request.from, request.to)).adaptError {
      case e => RemoteClientError(e.getMessage)
    }

}
