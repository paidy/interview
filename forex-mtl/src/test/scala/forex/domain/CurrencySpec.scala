package forex.domain

import forex.model.domain.Currency
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

  it should "generate all currency pairs" in {
    Currency.allCurrencyPairs.size shouldEqual 72
    Currency.allCurrencyPairs.foreach { v =>
      v.from should not be equals(v.to)
    }
  }
}
