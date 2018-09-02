package forex

import forex.domain.Currency

package object rates {

  final case class GetRatesRequest(
      from: Currency,
      to: Currency
  )

}
