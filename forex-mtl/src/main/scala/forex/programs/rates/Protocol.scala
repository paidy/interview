package forex.programs.rates

import forex.domain.Currency

object Protocol {

  final case class GetRatesRequest(
      from: Option[Currency],
      to: Option[Currency]
  )

}
