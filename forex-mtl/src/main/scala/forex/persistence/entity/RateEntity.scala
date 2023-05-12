package forex.persistence.entity

import forex.domain.{ Currency, Price, Timestamp }

case class RateEntity(to: Currency, from: Currency, price: Price, timestamp: Timestamp)
