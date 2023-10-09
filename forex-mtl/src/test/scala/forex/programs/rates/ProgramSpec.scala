package forex.programs.rates

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.typesafe.scalalogging.LazyLogging
import forex.clients.RatesClient
import forex.model.config.ProgramConfig
import forex.model.domain.{Currency, Price, Rate, Timestamp}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import forex.model.http.Protocol._
import scala.concurrent.duration._
import cats.implicits._
import forex.model.errors.RateErrors.{OneFrameCallFailed, RateNotFound}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper


class ProgramSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers with AsyncMockFactory with LazyLogging {

  val config: ProgramConfig = ProgramConfig(
    oneFrameToken = "token_1",
    cacheExpireTimeout = 2.seconds
  )

  def fakeOneFrameRate(from: Currency.Value, to: Currency.Value): OneFrameRate =
    OneFrameRate(from, to, BigDecimal(1), BigDecimal(2), Price(3), Timestamp.now())

  it should "return rate data if found and refresh cache" in {
    val ratesClient: RatesClient[IO] = mock[RatesClient[IO]]
    val oneFrameService = new Program[IO](config, ratesClient)

    val fakeRate1 = fakeOneFrameRate(Currency.CAD, Currency.CHF)
    val fakeRate2 = fakeOneFrameRate(Currency.NZD, Currency.USD)

    (ratesClient.get(_: Set[Rate.Pair], _: String))
      .expects(Currency.allCurrencyPairs, config.oneFrameToken)
      .once()
      .returns(IO.pure(List(fakeRate1, fakeRate2)))

    List(fakeRate1, fakeRate2).map(fakeRate =>
      oneFrameService.get(fakeRate.from, fakeRate.to)
      .map { res =>
        logger.info(s"Run for $fakeRate, result: $res")

        res.from      shouldEqual fakeRate.from
        res.to        shouldEqual fakeRate.to
        res.price     shouldEqual fakeRate.price
        res.timestamp shouldEqual fakeRate.timeStamp
      })
      .sequence.map(_ => ())
  }

  it should "after data expired should refresh them again" in {
    val ratesClient: RatesClient[IO] = mock[RatesClient[IO]]
    val oneFrameService = new Program[IO](config, ratesClient)

    val fakeRate = fakeOneFrameRate(Currency.CAD, Currency.CHF)

    (ratesClient.get(_: Set[Rate.Pair], _: String))
      .expects(Currency.allCurrencyPairs, config.oneFrameToken)
      .twice()
      .returns(IO.pure(List(fakeRate)))

    for {
      _ <- oneFrameService.get(fakeRate.from, fakeRate.to)     // First call result
      _ <- IO.sleep(config.cacheExpireTimeout.plus(1.seconds)) // Wait for cached data expire
      _ <- oneFrameService.get(fakeRate.from, fakeRate.to)     // Second call result
    } yield ()
  }

  it should "return NotFound if rate data not found" in {
    val ratesClient: RatesClient[IO] = mock[RatesClient[IO]]
    val oneFrameService = new Program[IO](config, ratesClient)

    (ratesClient.get(_: Set[Rate.Pair], _: String))
      .expects(Currency.allCurrencyPairs, config.oneFrameToken)
      .once()
      .returns(IO.pure(List())) // No data loaded form OneFrame

    oneFrameService.get(Currency.CAD, Currency.CHF)
      .recover[Any] { error =>
        logger.info(s"NotFound error: $error")

        error mustBe a[RateNotFound]
      }
      .map(_ => ())
  }

  it should "return ServiceUnavailable if OneFrame call failed" in {
    val ratesClient: RatesClient[IO] = mock[RatesClient[IO]]
    val oneFrameService = new Program[IO](config, ratesClient)

    (ratesClient.get(_: Set[Rate.Pair], _: String))
      .expects(Currency.allCurrencyPairs, config.oneFrameToken)
      .once()
      .returns(IO.raiseError(new RuntimeException("Ops! OneFrame down")))

    oneFrameService.get(Currency.CAD, Currency.CHF)
      .recover[Any] { error =>
        logger.info(s"ServiceUnavailable error: $error")

        error mustBe a[OneFrameCallFailed]
      }
      .map(_ => ())
  }
}
