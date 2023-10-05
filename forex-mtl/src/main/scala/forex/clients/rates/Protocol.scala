package forex.clients.rates

import forex.domain.{Currency, Price, Timestamp}
import io.circe.generic.extras.Configuration

object Protocol {

  implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

  final case class OneFrameRate(
                                 from: Currency.Value,
                                 to: Currency.Value,
                                 bid: BigDecimal,
                                 ask: BigDecimal,
                                 price: Price,
                                 timestamp: Timestamp
                               )
}
