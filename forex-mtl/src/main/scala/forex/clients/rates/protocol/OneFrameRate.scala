package forex.clients.rates.protocol

import forex.domain.{ Currency, Price, Timestamp }

case class OneFrameRate(from: Currency, to: Currency, bid: Price, ask: Price, price: Price, timeStamp: Timestamp)
