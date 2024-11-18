package forex.programs.rates

import forex.domain.Currency

object Protocol {

  final case class GetRatesRequest(
      from: Currency,
      to: Currency

      //TODO:: Fetch the values from Redis or DB...
  )

}
