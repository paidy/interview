package forex.rates
package program

import algebra.Rates
import cats.MonadError
import cats.syntax.monadError._
import errors._
import forex.domain.Rate
import forex.client.algebra.OneForgeClient

class RatesProgram[F[_]: MonadError[?[_], Throwable]](
    oneForge: OneForgeClient[F]
) extends Rates[F] {

  override def get(request: GetRatesRequest): F[Rate] =
    oneForge.get(Rate.Pair(request.from, request.to)).adaptError {
      case e => RateError.RemoteClientError(e.getMessage)
    }

}
