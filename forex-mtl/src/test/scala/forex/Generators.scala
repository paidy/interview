package forex


import cats.data.NonEmptyList

import java.time.OffsetDateTime
import org.scalacheck.Gen
import forex.domain._
import forex.domain.HealthCheck.{AppStatus, RedisStatus, Status}
import forex.services.rates.Protocol.{ExchangeRate, OneFrameResponse}


object Generators {

  val statusGen: Gen[Status] = Gen.oneOf(Status.Unreachable, Status.OK)
  val redisStatusGen: Gen[RedisStatus] = statusGen.map(RedisStatus)
  val appStatusGen: Gen[AppStatus] = redisStatusGen.map(AppStatus)

  val currencyGen: Gen[Currency] = Gen.oneOf(Currency.values)
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
    exchangeRateGen.map(exchangeRate => OneFrameResponse(NonEmptyList.fromListUnsafe(exchangeRate :: Nil)))

  val randomMsgGen: Gen[String] = Gen.asciiPrintableStr

  val oneFrameResponseWithRandomMsgGen: Gen[(OneFrameResponse, String)] =
    for {
      response <- oneFrameResponseGen
      msg <- randomMsgGen
    } yield (response, msg)

  val oneFrameResponseManyGen: Gen[OneFrameResponse] =
    Gen.nonEmptyListOf(exchangeRateGen).map {
      exchangeRates => OneFrameResponse(NonEmptyList.fromListUnsafe(exchangeRates))
    }

}
