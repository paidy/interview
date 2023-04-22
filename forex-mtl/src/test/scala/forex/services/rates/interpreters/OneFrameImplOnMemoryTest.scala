package forex.services.rates.interpreters

import org.scalatest.matchers.should.Matchers
import cats.effect.IO
import cats.implicits.toTraverseOps
import forex.domain.model.{ Currency, Price, Rate, Timestamp }
import forex.http.external.oneframe.OneFrameClient
import org.scalatest.funspec.AnyFunSpec

class OneFrameImplOnMemoryTest extends AnyFunSpec with Matchers {

  class TestOneFrameClient extends OneFrameClient[IO] {
    override def getRates(pairs: Seq[Rate.Pair]): IO[Seq[Rate]] =
      pairs.traverse { pair =>
        val rate = Rate(pair, Price(BigDecimal(100)), Timestamp.now)
        IO.pure(rate)
      }
  }

  describe("OneFrameImplOnMemory") {
    it("return cached rate if it exists") {
      val client               = new TestOneFrameClient()
      val oneFrameImplOnMemory = new OneFrameImplOnMemory[IO](client)

      val testPair = Rate.Pair(Currency.USD, Currency.JPY)

      for {
        // First, get the rate to ensure it's cached
        _ <- oneFrameImplOnMemory.get(testPair)

        // Now, get the rate again to test if it returns the cached rate
        result <- oneFrameImplOnMemory.get(testPair)
      } yield {
        result match {
          case Right(rate) =>
            rate.pair shouldBe testPair
            Cache.concurrentHashMap.contains(testPair) shouldBe true
          case Left(_) =>
            fail("Error should not be returned")
        }
      }
    }
  }

  // Additional tests...
}
