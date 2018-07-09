package forex.processes.rates

import java.time.OffsetDateTime

import cats.Id
import cats.syntax.either._
import forex.domain.Currency.{JPY, USD}
import forex.domain.{Price, Rate, Timestamp}
import forex.processes.rates.messages.Error.CurrentRateNotAvailable
import forex.processes.rates.messages.GetRequest
import forex.services._
import forex.processes._
import forex.services.oneforge.Error.Generic
import org.scalatest.{Matchers, WordSpec}

import scala.collection.mutable
import scala.concurrent.duration._

class ProcessesSpec extends WordSpec with Matchers {

  "Rates Process" when {
    "updating rates" should {
      val supportedPairs = Set(Rate.Pair(USD, JPY))
      "update cached rates with results from one forge client" in {
        val cut = Processes[Id]
        val defaultResult = supportedPairs
          .map(pair => Rate(pair, Price(BigDecimal(10)), Timestamp.now))

        implicit val mockedOneForge = new OneForge[Id] {
          override def get(pairs: Set[Rate.Pair]): Id[Either[OneForgeError, Set[Rate]]] =
            defaultResult.asRight[OneForgeError]
        }

        implicit val mockedRatesCache = emptyCache

        cut.updateRates(supportedPairs) shouldEqual Right(())

        mockedRatesCache.cache shouldEqual defaultResult
          .map(rate => (rate.pair, rate))
          .toMap
      }

      "return process error when was unable to fetch new rates" in {
        val cut = Processes[Id]

        implicit val mockedOneForge = new OneForge[Id] {
          override def get(pairs: Set[Rate.Pair]): Id[Either[OneForgeError, Set[Rate]]] =
            Generic.asLeft[Set[Rate]]
        }

        implicit val mockedRatesCache = emptyCache

        cut.updateRates(supportedPairs) shouldEqual Left(RatesError.Generic)

        mockedRatesCache.cache.isEmpty shouldBe true
      }
    }

    "handling get request" should {
      val getRequest = GetRequest(USD, JPY)
      val cachedRate =
        Rate(
          Rate.Pair(getRequest.from, getRequest.to),
          Price(BigDecimal(100)),
          Timestamp(OffsetDateTime.now().minusMinutes(10))
        )

      "return CurrentRateNotAvailable exception when doesn't have rate for provided pair" in {
        val cut = Processes[Id]

        implicit val mockedRatesCache = emptyCache

        cut.get(
          getRequest,
          10.minutes
        ) shouldEqual CurrentRateNotAvailable.asLeft[Rate]
      }

      "return CurrentRateNotAvailable exception when cached rate is older than provided max rate age" in {
        val cut = Processes[Id]

        implicit val mockedRatesCache = emptyCache
        mockedRatesCache.cache.update(cachedRate.pair, cachedRate)

        cut.get(
          getRequest,
          5.minutes
        ) shouldEqual CurrentRateNotAvailable.asLeft[Rate]
      }

      "return cached rate if it's not older than provided max rate age" in {
        val cut = Processes[Id]

        implicit val mockedRatesCache = emptyCache
        mockedRatesCache.cache.update(cachedRate.pair, cachedRate)

        cut.get(
          getRequest,
          15.minutes
        ) shouldEqual cachedRate.asRight[RatesError]
      }
    }
  }

  private def emptyCache = new MockedRatesCache()

  private class MockedRatesCache extends RatesCache[Id] {
    val cache = mutable.HashMap.empty[Rate.Pair, Rate]

    override def get(pair: Rate.Pair): Id[Option[Rate]] =
      cache.get(pair)

    override def store(rates: Set[Rate]): Id[Unit] =
      rates.foreach(rate => cache.update(rate.pair, rate))
  }
}
