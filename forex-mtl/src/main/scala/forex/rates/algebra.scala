package forex.rates

import forex.domain.Rate

object algebra {

  trait Rates[F[_]] {
    def get(request: GetRatesRequest): F[Rate]
  }

}
