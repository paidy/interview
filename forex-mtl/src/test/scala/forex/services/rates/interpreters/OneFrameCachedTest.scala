package forex.services.rates.interpreters


import cats.Applicative
import cats.syntax.all._
import cats.effect.concurrent.{Deferred, Ref, Semaphore}
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{Concurrent, ContextShift, ExitCode, IO, IOApp}
import cats.effect.syntax.all._
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId}
import forex.domain.Currency.{JPY, USD, currencies}
import forex.domain.Rate.Pair
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.BatchedAlgebra
import forex.services.rates.errors.Error
import forex.services.rates.interpreters.OneFrameCached.{CachedRate, allCurrencyCombinations, ratesToCacheMap}
import forex.services.rates.interpreters.OneFrameCachedTest.{OneFrameBatchedStub, generateRates, generateStaleCacheMap, instance}
import org.scalatest.{Failed, Succeeded}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.{Duration, Instant, OffsetDateTime}
import scala.concurrent.Future
import scala.util.Random

class OneFrameCachedTest extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "OneFrameCached" - {
    "should return the value from OneFrameBatched when cache is cold" in {
      val resultIO = for {
        testClientData <- instance()
        result <- testClientData.client.get(Pair(JPY, USD))
      } yield (result, testClientData)

      val assertions = for {
        result <- resultIO
        count <- result._2.callCount.get
        cache <- result._2.cachedResults.get
      } yield Seq(
        count shouldBe 1,
        cache.isEmpty shouldBe false,
        result._1.isRight shouldBe true,
        result._1.map(rate => rate.pair shouldBe Pair(from = JPY, to = USD)).getOrElse(true shouldBe false),
      )

      assertions.asserting(_.forall(result => result == Succeeded) shouldBe true)
    }

    "should return the value from OneFrameBatched when cached value is stale" in {
      val resultIO = for {
        testClientData <- instance()
        _ <- testClientData.cachedResults.set(generateStaleCacheMap(allCurrencyCombinations))
        result <- testClientData.client.get(Pair(JPY, USD))
      } yield (result, testClientData)

      val assertions = for {
        result <- resultIO
        count <- result._2.callCount.get
        cache <- result._2.cachedResults.get
      } yield Seq(
        count shouldBe 1,
        cache.isEmpty shouldBe false,
        cache.values forall (_.cacheTime.isAfter(Instant.now().minus(Duration.ofMinutes(5L)))) shouldBe true,
        result._1.isRight shouldBe true,
        result._1.map(rate => rate.pair shouldBe Pair(from = JPY, to = USD)).getOrElse(true shouldBe false),
      )

      assertions.asserting(_.forall(result => result == Succeeded) shouldBe true)
    }

    "should return the value from cache when cached value is fresh" in {
      val resultIO = for {
        testClientData <- instance()
        _ <- testClientData.cachedResults.set(ratesToCacheMap(generateRates(allCurrencyCombinations)))
        result <- testClientData.client.get(Pair(JPY, USD))
      } yield (result, testClientData)

      val assertions = for {
        result <- resultIO
        count <- result._2.callCount.get
        cache <- result._2.cachedResults.get
      } yield Seq(
        count shouldBe 0,
        cache.isEmpty shouldBe false,
        result._1.isRight shouldBe true,
        result._1.map(rate => rate.pair shouldBe Pair(from = JPY, to = USD)).getOrElse(true shouldBe false),
      )

      assertions.asserting(_.forall(result => result == Succeeded) shouldBe true)
    }

    "should only fetch the value from OneFrameBatched once when cached value is stale" in {
      val callCountIO = for {
        testClientData <- instance()

        // Concurrent requests
        _ <- Seq.range(1,8).map(_ => testClientData.client.get(Pair(JPY, USD))).parSequence
        callCount <- testClientData.callCount.get
      } yield (callCount)

      callCountIO.asserting(_ shouldBe 1)
    }

  }

  implicit val cs: ContextShift[IO] = IO.contextShift(this.executionContext)
}

object OneFrameCachedTest {

  private def instance()(implicit cs: ContextShift[IO]): IO[TestClientData] = {
    for {
      semaphore <- Semaphore.apply[IO](1)
      cachedResults <- Ref.of[IO, Map[Pair, CachedRate]](Map())
      callCount <- Ref.of[IO, Int](0)
      client = new OneFrameCached[IO](
        new OneFrameBatchedStub(
          for {
            _ <- callCount.modify(count => (count + 1, count))
          } yield generateRatesEither
        ),
        cachedResults,
        semaphore
      )
    } yield TestClientData(client, cachedResults, callCount)
  }

  private def generateRatesEither(pairs: Seq[Rate.Pair]): Error Either Seq[Rate] = {
    generateRates(pairs).asRight[Error]
  }

  private def generateRates(pairs: Seq[Rate.Pair]): Seq[Rate] = {
    pairs.map(pair => Rate(
      pair = pair,
      price = new Price(Random.nextInt(99) + 1),
      timestamp = Timestamp(OffsetDateTime.now())
    ))
  }

  private def generateStaleCacheMap(pairs: Seq[Rate.Pair]): Map[Pair, CachedRate] = {
    ratesToCacheMap(generateRates(pairs))
      .map(cachedRate => cachedRate.copy(
        _2 = cachedRate._2.copy(cacheTime = Instant.now().minus(Duration.ofMinutes(6L)))
      )
    )
  }

  private class OneFrameBatchedStub[F[_]: Applicative](private val generateRates: F[(Seq[Rate.Pair]) => Error Either Seq[Rate]]) extends BatchedAlgebra[F] {
    override def get(pairs: Seq[Rate.Pair]): F[Error Either Seq[Rate]] = {
      for {
        rateGenerator <- generateRates
      } yield (rateGenerator(pairs))
    }
  }

  private case class TestClientData(
    client: OneFrameCached[IO],
    cachedResults: Ref[IO, Map[Pair, CachedRate]],
    callCount: Ref[IO, Int]
  )
}
