package forex.programs.rates

import cats.Show
import cats.effect._
import weaver.SimpleIOSuite
import weaver.scalacheck.Checkers
import forex.domain.Rate
import forex.services.{CacheService, RatesService}
import forex.services.rates.errors
import forex.Generators._
import forex.programs.rates.Protocol.GetRatesRequest
import forex.programs.rates.errors.Error.RateLookupFailed
import org.typelevel.log4cats.noop.NoOpLogger


object RatesProgramSuite extends SimpleIOSuite with Checkers {

  implicit val logger = NoOpLogger[IO]

  implicit val showAppStatus: Show[Rate] = Show.fromToString

  def successRateService(rate: Rate): RatesService[IO] =
    new RatesService[IO] {
      override def get(pair: Rate.Pair): IO[Either[errors.Error, Rate]] = IO.pure(Right(rate))
    }

  def failedRateService: RatesService[IO] =
    new RatesService[IO] {
      override def get(pair: Rate.Pair): IO[Either[errors.Error, Rate]] =
        IO.pure(Left(errors.Error.OneFrameLookupFailed("Dummy failed")))
    }

  def successCacheService(rate: Rate): CacheService[IO] =
    new CacheService[IO] {
      override def get(pair: Rate.Pair): IO[Option[Rate]] = IO.pure(Some(rate))
      override def set(pair: Rate.Pair, rate: Rate): IO[Boolean] = IO.pure(true)
    }

  def failedCacheService: CacheService[IO] =
    new CacheService[IO] {
      override def get(pair: Rate.Pair): IO[Option[Rate]] = IO.pure(None)
      override def set(pair: Rate.Pair, rate: Rate): IO[Boolean] = IO.pure(true)
    }

  test("Cache: O, Service: X should return the rate") {
    forall(rateGen) { expectRate =>
      val req = GetRatesRequest(
        expectRate.pair.from,
        expectRate.pair.to
      )
      Program[IO](
        failedRateService,
        successCacheService(expectRate)
      ).get(req)
        .map {
          case Right(actualRate) =>
            expect.same(expectRate, actualRate)
          case _ => failure("Cache: O, Service: X test failed")
        }
    }
  }

  test("Cache: X, Service: O should return the rate ") {
    forall(rateGen) { expectRate =>
      val req = GetRatesRequest(
        expectRate.pair.from,
        expectRate.pair.to
      )
      Program[IO](
        successRateService(expectRate),
        failedCacheService
      ).get(req)
        .map {
          case Right(actualRate) =>
            expect.same(expectRate, actualRate)
          case _ => failure("Cache: X, Service: O test failed")
        }
    }
  }

  test("Cache: X, Service: X cannot return the rate ") {
    forall(rateGen) { expectRate =>
      val req = GetRatesRequest(
        expectRate.pair.from,
        expectRate.pair.to
      )
      Program[IO](
        failedRateService,
        failedCacheService
      ).get(req)
        .map {
          case Left(err) =>
            expect.same(err, RateLookupFailed("Dummy failed"))
          case _ => failure("Cache: X, Service: X test failed")
        }
    }
  }

}
