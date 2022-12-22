package forex


import java.time.OffsetDateTime
import org.scalacheck.Gen
import forex.domain._
import forex.domain.HealthCheck.{AppStatus, RedisStatus, Status}
import forex.services.rates.Protocol.{ExchangeRate, OneFrameResponse}


object Generators {

  val statusGen: Gen[Status] = Gen.oneOf(Status.Unreachable, Status.OK)
  val redisStatusGen: Gen[RedisStatus] = statusGen.map(RedisStatus)
  val appStatusGen: Gen[AppStatus] = redisStatusGen.map(AppStatus)

  val currencyGen: Gen[Currency] = Gen.oneOf( // Currency could be a Enum
    Currency.AUD,
    Currency.CAD,
    Currency.CHF,
    Currency.EUR,
    Currency.GBP,
    Currency.NZD,
    Currency.JPY,
    Currency.SGD,
    Currency.USD
  )
  val pairGen: Gen[Rate.Pair] =
    for {
      a <- currencyGen
      b <- currencyGen
    } yield Rate.Pair(a, b)

  val priceGen: Gen[Price] = Gen.posNum[BigDecimal].map(Price.apply)
  val timestampGen: Gen[Timestamp] = Gen.choose(OffsetDateTime.MIN, OffsetDateTime.MAX).map(Timestamp.apply)
  val rateGen: Gen[Rate] =
    for {
      pair <- pairGen
      price <- priceGen
      timestamp <- timestampGen
    } yield Rate(pair, price, timestamp)

  val exchangeRateGen: Gen[ExchangeRate] =
    for {
      from <- currencyGen
      to <- currencyGen
      bid <- Gen.posNum[BigDecimal]
      ask <- Gen.posNum[BigDecimal]
      price <- priceGen
      timeStamp <- timestampGen
    } yield ExchangeRate(from, to, bid, ask, price, timeStamp)

  val oneFrameResponseGen: Gen[OneFrameResponse] =
    exchangeRateGen.map(exchangeRate => OneFrameResponse(exchangeRate :: Nil))
}
