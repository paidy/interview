package forex.http.rates

import forex.domain.{ Currency, Price, Rate, Timestamp }
import org.scalacheck.Gen

import java.time.{ Instant, LocalDate, OffsetDateTime, ZoneOffset }

object Generator {

  val MaxTimeInMillis: Long = LocalDate.MAX.toEpochDay

  val DoubleGen: Gen[Double] = Gen.chooseNum(0, Double.MaxValue)

  val CurrencyGen: Gen[Currency] = Gen.oneOf(Currency.Values)

  val InstantGen: Gen[Instant] = Gen.calendar.map(_.toInstant)

  val OffsetDateTimeGen: Gen[OffsetDateTime] = InstantGen.map(_.atOffset(ZoneOffset.UTC))

  val TimestampGen: Gen[Timestamp] = OffsetDateTimeGen.map(Timestamp(_))

  val PriceGen: Gen[Price] = DoubleGen.map(BigDecimal(_)).map(Price(_))

  val RatePairGen: Gen[Rate.Pair] =
    for {
      from <- CurrencyGen
      to <- CurrencyGen
    } yield {
      Rate.Pair(from, to)
    }

  val RateGen: Gen[Rate] = for {
    pair <- RatePairGen
    price <- PriceGen
    timestamp <- TimestampGen

  } yield {
    Rate(pair, price, timestamp)
  }

}