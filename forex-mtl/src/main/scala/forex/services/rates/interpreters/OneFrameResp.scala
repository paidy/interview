package forex.services.rates.interpreters

import io.circe.Decoder

case class OneFrameResp(
                         from: String,
                         to: String,
                         bid: BigDecimal,
                         ask: BigDecimal,
                         price: BigDecimal,
                         timestamp: String
                       )

object OneFrameResp {
  implicit val oneFrameRespDecoder: Decoder[OneFrameResp] =
    Decoder.forProduct6(
      "from",
      "to",
      "bid",
      "ask",
      "price",
      "time_stamp"
    )(OneFrameResp.apply)
}
