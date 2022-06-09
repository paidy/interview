package forex.http.rates

import forex.domain.Rate
import forex.http.rates.Generator.RateGen
import forex.http.rates.Protocol._
import io.circe.syntax.EncoderOps
import org.scalatest.matchers.should.Matchers
import org.scalatest.propspec.AnyPropSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class ConvertersSpec extends AnyPropSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  property("convert Rate to json") {

    forAll(RateGen) { rate =>
      val json = rate.asJson
      json.as[Rate] shouldBe Right(rate)
    }
  }
}
