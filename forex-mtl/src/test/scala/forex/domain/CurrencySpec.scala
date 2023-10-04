package forex.domain

import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class CurrencySpec extends AnyFlatSpec with Matchers with TryValues {

  it should "build from string" in {
    Currency.values.foreach { v =>
      Currency.fromString(v.toString).success.value shouldEqual v
    }
  }

  it should "fail if string name incorrect" in {
    Currency.fromString("invalid_value").failure
  }
}
