package forex.programs.rates

import forex.domain.model.Currency

object Protocol {

  final case class GetRatesRequest(
      from: Currency,
      to: Currency
  )

}
