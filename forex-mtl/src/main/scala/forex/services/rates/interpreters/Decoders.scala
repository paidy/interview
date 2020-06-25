package forex.services.rates.interpreters

import java.time.OffsetDateTime

import cats.effect.IO
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.services.rates.errors.Error
import forex.services.rates.errors.Error.OneFrameLookupFailed
import io.circe.{ Decoder, HCursor }
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

import scala.util.Try

object Decoders {

  implicit val currencyDecoder: Decoder[Currency] = Decoder.decodeString.emapTry(str => Try(Currency.fromString(str).get))
  implicit val timeStampDecoder: Decoder[OffsetDateTime] =
    Decoder.decodeString.emapTry(str => Try(OffsetDateTime.parse(str)))

  val rateDecoder: Decoder[Rate] = (c: HCursor) =>
    for {
      from <- c.downField("from").as[Currency]
      to <- c.downField("to").as[Currency]
      price <- c.downField("price").as[BigDecimal]
      timestamp <- c.downField("time_stamp").as[OffsetDateTime]
    } yield {
      Rate(Rate.Pair(from, to), Price(price), Timestamp(timestamp))
  }

  val errorDecoder: Decoder[Error] = (c: HCursor) =>
    for {
      error <- c.downField("error").as[String]
    } yield OneFrameLookupFailed(error)

  implicit val eitherDecoder: Decoder[Error Either Seq[Rate]] = errorDecoder.either(Decoder.decodeSeq(rateDecoder))

  implicit val rateEntityDecoder: EntityDecoder[IO, Error Either Seq[Rate]] = jsonOf[IO, Error Either Seq[Rate]]

}
