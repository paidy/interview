package forex.services.rates


import forex.config
import forex.config.{HttpConfig, OneFrameConfig}
import forex.domain.Currency
import forex.domain.Rate.Pair
import forex.services.RatesServices
import forex.services.rates.errors.RateServiceError
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
//import org.scalatest.matchers.should.Matchers._

import scala.concurrent.duration.DurationInt

class OneFrameLiveSpec extends AnyWordSpec with Matchers {


  "One Frame Service" should {
    "return a Right when everything is okay" in {
      val oneFrameToken = "10dc303535874aeccc86a8251e6992f5"
      val oneFrameConfig = OneFrameConfig(HttpConfig("localhost", 8080, 40.seconds), oneFrameToken)
      val redisConfig = config.RedisConfig("localhost", 6379, 5.minutes)

      val rs: Algebra[Option] = RatesServices.live(oneFrameConfig, redisConfig)

      val pair = Pair(Currency.USD, Currency.JPY)
      val result = rs.get(pair)
//      println(result)
      result shouldBe a [Some[_]]
      result.get shouldBe a [Right[_, _]]
    }

    "return an Error when calling with wrong " in {
      val wrongToken = "aaaaaaaaaaaaaaaa"
      val oneFrameConfig = OneFrameConfig(HttpConfig("localhost", 8080, 40.seconds), wrongToken)
      val redisConfig = config.RedisConfig("localhost", 6379, 5.minutes)

      val rs: Algebra[Option] = RatesServices.live(oneFrameConfig, redisConfig)

      val pair = Pair(Currency.USD, Currency.JPY)
      val result = rs.get(pair)
      //      println(result)
      result shouldBe a [Some[_]]
      result.get shouldBe a [RateServiceError]
    }
  }
}