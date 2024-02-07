package forex.services.rates


import forex.config.{HttpConfig, OneFrameConfig, RedisConfig}
import forex.domain.Currency
import forex.domain.Rate.Pair
import forex.services.RatesServices
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
//import org.scalatest.matchers.should.Matchers._

import scala.concurrent.duration.DurationInt

class OneFrameLiveSpec extends AnyWordSpec with Matchers {
  val oneFrameConfig = OneFrameConfig(HttpConfig("localhost", 8080, 40.seconds), "10dc303535874aeccc86a8251e6992f5")
  val redisConfig = RedisConfig("localhost", 6379, 5.minutes)


  val rs: Algebra[Option] = RatesServices.live(oneFrameConfig, redisConfig)

  "One Frame Service" should {
    "return a Right when calling rs.get(pair)" in {
      val pair = Pair(Currency.USD, Currency.JPY)
      val result = rs.get(pair)
//      println(result)
      result shouldBe a [Some[_]]
      result.get shouldBe a [Right[_, _]]
    }
  }
}