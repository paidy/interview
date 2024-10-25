package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.implicits.{catsSyntaxEitherId, toBifunctorOps}
import forex.client.OneFrameHttpClient
import forex.domain.{Price, Rate, Timestamp}
import io.circe.parser._
import forex.services.rates.errors._
import io.circe.{Decoder, HCursor}

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

case class CurrencyInfo(from: String, to: String, bid: Double, ask: Double, price: Double, time_stamp: String)

class OneFrameClient[F[_]: Applicative](oneFrameHttpClient: OneFrameHttpClient) extends Algebra[F] {
  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    getApiResponse(pair)
  }

  def getApiResponse(pair: Rate.Pair): F[Error Either Rate] = {
    val res = oneFrameHttpClient.getRates(pair)

    res.fold(
      e => Applicative[F].pure(Error.OneFrameLookupFailed(e).asLeft[Rate]),
      r => {
        val parsedResult: Either[Error, Rate] = for {
          json <- parse(r.body).leftMap(err => Error.JsonParsingFailed(err.getMessage))
          element <- json.as[List[CurrencyInfo]].leftMap(err => Error.JsonDecodingFailed(err.getMessage()))
            .map(x => x.head)
        } yield Rate(
          pair,
          Price(BigDecimal(element.price)),
          new Timestamp(OffsetDateTime.parse(element.time_stamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        )

        Applicative[F].pure(parsedResult)
      }
    )
  }

  // Define a Circe Decoder for the case class
  implicit val currencyInfoDecoder: Decoder[CurrencyInfo] = (c: HCursor) =>
    for {
      from <- c.downField("from").as[String]
      to <- c.downField("to").as[String]
      bid <- c.downField("bid").as[Double]
      ask <- c.downField("ask").as[Double]
      price <- c.downField("price").as[Double]
      time_stamp <- c.downField("time_stamp").as[String]
    } yield CurrencyInfo(from, to, bid, ask, price, time_stamp)
}
