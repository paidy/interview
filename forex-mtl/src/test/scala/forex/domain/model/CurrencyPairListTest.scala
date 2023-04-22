package forex.domain.model

import forex.domain.model.Currency._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class CurrencyPairListTest extends AnyFunSpec with Matchers {
  describe("CurrencyPairList") {
    val currencyPairList = CurrencyPairList.all
    it("should have 72 pairs") {
      currencyPairList.size shouldBe 72
      val expectedPairs = List(
        Rate.Pair(AUD, CAD),
        Rate.Pair(AUD, CHF),
        Rate.Pair(AUD, EUR),
        Rate.Pair(AUD, GBP),
        Rate.Pair(AUD, NZD),
        Rate.Pair(AUD, JPY),
        Rate.Pair(AUD, SGD),
        Rate.Pair(AUD, USD),
        Rate.Pair(CAD, AUD),
        Rate.Pair(CAD, CHF),
        Rate.Pair(CAD, EUR),
        Rate.Pair(CAD, GBP),
        Rate.Pair(CAD, NZD),
        Rate.Pair(CAD, JPY),
        Rate.Pair(CAD, SGD),
        Rate.Pair(CAD, USD),
        Rate.Pair(CHF, AUD),
        Rate.Pair(CHF, CAD),
        Rate.Pair(CHF, EUR),
        Rate.Pair(CHF, GBP),
        Rate.Pair(CHF, NZD),
        Rate.Pair(CHF, JPY),
        Rate.Pair(CHF, SGD),
        Rate.Pair(CHF, USD),
        Rate.Pair(EUR, AUD),
        Rate.Pair(EUR, CAD),
        Rate.Pair(EUR, CHF),
        Rate.Pair(EUR, GBP),
        Rate.Pair(EUR, NZD),
        Rate.Pair(EUR, JPY),
        Rate.Pair(EUR, SGD),
        Rate.Pair(EUR, USD),
        Rate.Pair(GBP, AUD),
        Rate.Pair(GBP, CAD),
        Rate.Pair(GBP, CHF),
        Rate.Pair(GBP, EUR),
        Rate.Pair(GBP, NZD),
        Rate.Pair(GBP, JPY),
        Rate.Pair(GBP, SGD),
        Rate.Pair(GBP, USD),
        Rate.Pair(NZD, AUD),
        Rate.Pair(NZD, CAD),
        Rate.Pair(NZD, CHF),
        Rate.Pair(NZD, EUR),
        Rate.Pair(NZD, GBP),
        Rate.Pair(NZD, JPY),
        Rate.Pair(NZD, SGD),
        Rate.Pair(NZD, USD),
        Rate.Pair(JPY, AUD),
        Rate.Pair(JPY, CAD),
        Rate.Pair(JPY, CHF),
        Rate.Pair(JPY, EUR),
        Rate.Pair(JPY, GBP),
        Rate.Pair(JPY, NZD),
        Rate.Pair(JPY, SGD),
        Rate.Pair(JPY, USD),
        Rate.Pair(SGD, AUD),
        Rate.Pair(SGD, CAD),
        Rate.Pair(SGD, CHF),
        Rate.Pair(SGD, EUR),
        Rate.Pair(SGD, GBP),
        Rate.Pair(SGD, NZD),
        Rate.Pair(SGD, JPY),
        Rate.Pair(SGD, USD),
        Rate.Pair(USD, AUD),
        Rate.Pair(USD, CAD),
        Rate.Pair(USD, CHF),
        Rate.Pair(USD, EUR),
        Rate.Pair(USD, GBP),
        Rate.Pair(USD, NZD),
        Rate.Pair(USD, JPY),
        Rate.Pair(USD, SGD)
      )

      CurrencyPairList.all should contain theSameElementsAs expectedPairs
    }
    it("should have 9 pairs with AUD") {
      currencyPairList.count(_.from == Currency.AUD) shouldBe 9
    }

  }
}
