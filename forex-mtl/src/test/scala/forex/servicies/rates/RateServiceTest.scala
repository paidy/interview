package forex.services.rates

import cats.effect.IO
import forex.domain._
import forex.services.rates.interpreters.OneFrameInterpreter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RateServiceTest extends AnyFlatSpec with Matchers {
  "RateService" should "retrieve a valid rate for a currency pair" in {
    val service = new OneFrameInterpreter[IO]()
    val result = service.get(Rate.Pair(Currency.USD, Currency.JPY)).unsafeRunSync()

    result should be a "Right"
    result.foreach { rate =>
      rate.pair.from shouldBe Currency.USD
      rate.pair.to shouldBe Currency.JPY
      rate.price.value should be > 0
    }
  }
}
