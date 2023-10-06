package forex.services.rates

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import forex.cache.RatesCache
import forex.clients.RatesClient
import forex.model.config.OneFrameServiceConfig
import forex.model.domain.{Currency, Price, Rate, Timestamp}
import forex.model.http.Protocol.OneFrameRate
import org.scalamock.matchers.ArgCapture.CaptureAll
import org.scalamock.scalatest.{AsyncMockFactory, MockFactory}
import org.scalatest.Suite
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import forex.model.http.Converters._


class OneFrameServiceSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers with AsyncMockFactory {

  val config: OneFrameServiceConfig = OneFrameServiceConfig(
    oneFrameTokens = List("token_1", "token_2"),
    ratesRefreshTimeout = 2.seconds
  )

  val fakeOneFrameRate: OneFrameRate = OneFrameRate(
    Currency.CAD, Currency.CHF, BigDecimal(1), BigDecimal(2), Price(3), Timestamp.now())

  def mockedRatesClient(): RatesClient[IO] = mock[RatesClient[IO]]

  def mockedRatesCache(): RatesCache[IO] = mock[RatesCache[IO]]

  it should "refresh rights with given timeout" in {
    val ratesClient = mockedRatesClient()
    val ratesCache = mockedRatesCache()

    val clientCapturePair = CaptureAll[Set[Rate.Pair]]()
    val clientCaptureTokens = CaptureAll[String]()

    (ratesClient.get(_: Set[Rate.Pair], _: String))
      .expects(capture(clientCapturePair), capture(clientCaptureTokens))
      .anyNumberOfTimes()
      .returns(IO.pure(List(fakeOneFrameRate)))

    (ratesCache.update(_: Seq[Rate]))
      .expects(List(fakeOneFrameRate.asRate))
      .anyNumberOfTimes()
      .returns(IO.pure(()))

    val oneFrameService = new OneFrameService[IO](config, ratesClient, ratesCache)

    oneFrameService.ratesRefresh
      .interruptAfter(7.seconds)
      .compile.drain
      .map { _ =>

        clientCapturePair.values shouldEqual List(
          Currency.allCurrencyPairs, Currency.allCurrencyPairs, Currency.allCurrencyPairs)

        clientCaptureTokens.values.toSet shouldEqual config.oneFrameTokens.toSet
    }
  }
}
