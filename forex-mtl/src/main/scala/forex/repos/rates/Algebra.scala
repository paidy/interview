package forex.repos.rates

import errors._
import forex.domain.Rate

trait Algebra[F[_]] {
  def getAllRates: F[Either[Error, Seq[Rate]]]
}
