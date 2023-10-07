package forex.programs.rates

import forex.model.domain.Currency
import org.http4s.Response


trait Algebra[F[_]] {
  def get(from: Currency.Value, to: Currency.Value): F[Response[F]]
}
