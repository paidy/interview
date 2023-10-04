package forex.programs.rates

import forex.domain.Currency

object Protocol {

  final case class GetRatesRequest(
      from: Currency.Value,
      to: Currency.Value
  )

}
