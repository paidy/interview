package forex.programs.rates

import cats.data.OptionT
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.typesafe.scalalogging.LazyLogging
import forex.cache.RatesCache
import forex.model.config.CacheConfig
import forex.model.domain.{Currency, Price, Rate, Timestamp}
import forex.model.http.Protocol.GetApiResponse
import org.http4s.Status
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import forex.model.http.Marshalling._
import forex.model.http.Converters._
import forex.model.http.Protocol._



class ProgramSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers with AsyncMockFactory with LazyLogging {

  val fakeRate: Rate = Rate(
    Rate.Pair(Currency.CAD, Currency.CHF), Price(123), Timestamp.now())

  def mockedRatesCache(): RatesCache[IO] = mock[RatesCache[IO]]

  it should "return OK response with rate data if found" in {
    val ratesCache = mockedRatesCache()

    (ratesCache.get(_: Rate.Pair))
      .expects(fakeRate.pair)
      .once()
      .returns(OptionT.some[IO](fakeRate))

    val oneFrameService = new Program[IO](ratesCache)

    oneFrameService.get(fakeRate.pair.from, fakeRate.pair.to)
      .flatMap { response =>
        logger.info("OK response: " + response)
        logger.info("OK body: " + response.as[String].unsafeRunSync())

        response.status shouldEqual Status.Ok

        response.as[GetApiResponse]
      }
      .map { entity =>

        entity.from      shouldEqual fakeRate.pair.from
        entity.to        shouldEqual fakeRate.pair.to
        entity.price     shouldEqual fakeRate.price
        entity.timestamp shouldEqual fakeRate.timestamp
      }
  }

  it should "return NotFound response if data found" in {
    val ratesCache = mockedRatesCache()

    (ratesCache.get(_: Rate.Pair))
      .expects(fakeRate.pair)
      .once()
      .returns(OptionT.none[IO, Rate])

    val oneFrameService = new Program[IO](ratesCache)

    oneFrameService.get(fakeRate.pair.from, fakeRate.pair.to)
      .map { response =>
        logger.info("NotFound response: " + response)
        logger.info("NotFound body: " + response.as[String].unsafeRunSync())

        response.status shouldEqual Status.NotFound
      }
  }
}
