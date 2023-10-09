package forex.programs.rates

import forex.model.domain.Currency
import forex.model.http.Protocol.GetApiResponse


trait Algebra[F[_]] {
  def get(from: Currency.Value, to: Currency.Value): F[GetApiResponse]
}
