package paidy

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import scala.util.Random

import scala.util.control.NonFatal

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.commons.validation._

final class ForexSimulation extends Simulation {
  private val localPort = sys.env.get("FOREX_LOCAL_PORT").getOrElse("8080")

  val httpProtocol = http.baseUrl(s"http://localhost:$localPort")

  val scn = scenario("Forex").
    feed(currencyConversions).
      exec(http("Currency conversion from ${from} to ${to}")
        .get("/rates")
        .queryParam("from", "${from}")
        .queryParam("to", "${to}")
        .check(status.find.in(
          200, 400/* Bad request for unsupported currencies */))
        .check(checkIf((r: Response, _) => r.status.code == 200) {
          jsonPath("$.timestamp").ofType[String].
            transform(ZonedDateTime.parse(_: String)).
            validate("timestamp", { (optTs, session) =>
              optTs match {
                case Some(resultFreshness) => {
                  val _5minAgo = ZonedDateTime.now().minus(
                    5, ChronoUnit.MINUTES).
                    minus(500, ChronoUnit.MILLISECONDS) // precision workaround

                  if (resultFreshness.equals(_5minAgo) ||
                    resultFreshness.isAfter(_5minAgo)) {

                    Success(optTs)
                  } else {
                    Failure(s"timestamp ${resultFreshness} < ${_5minAgo}")
                  }
                }

                case _ =>
                  Success(optTs)
              }
            })
        })
      )

  setUp(scn.inject(constantUsersPerSec(100) during(17.minutes)).protocols(httpProtocol))

  private lazy val currencyConversions = currencies.combinations(2).collect {
    case from :: to :: _ => Map("from" -> from, "to" -> to)
  }.toIndexedSeq.random

  private lazy val currencies = Seq(
    "AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN",
    "BAM", "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BOV", "BRL", "BSD", "BTN", "BWP", "BYN", "BZD",
    "CAD", "CDF", "CHE", "CHF", "CHW", "CLF", "CLP", "CNY", "COP", "COU", "CRC", "CUC", "CUP", "CVE", "CZK",
    "DJF", "DKK", "DOP", "DZD",
    "EGP", "ERN", "ETB", "EUR",
    "FJD", "FKP",
    "GBP", "GEL", "GHS", "GIP", "GMD", "GNF", "GTQ", "GYD",
    "HKD", "HNL", "HRK", "HTG", "HUF",
    "IDR", "ILS", "INR", "IQD", "IRR", "ISK",
    "JMD", "JOD", "JPY",
    "KES", "KGS", "KHR", "KMF", "KPW", "KRW", "KWD", "KYD", "KZT",
    "LAK", "LBP", "LKR", "LRD", "LSL", "LYD",
    "MAD", "MDL", "MGA", "MKD", "MMK", "MNT", "MOP", "MRU", "MUR", "MVR", "MWK", "MXN", "MXV", "MYR", "MZN",
    "NAD", "NGN", "NIO", "NOK", "NPR", "NZD",
    "OMR",
    "PAB", "PEN", "PGK", "PHP", "PKR", "PLN", "PYG",
    "QAR",
    "RON", "RSD", "RUB", "RWF",
    "SAR", "SBD", "SCR", "SDG", "SEK", "SGD", "SHP", "SLL", "SOS", "SRD", "SSP", "STN", "SVC", "SYP", "SZL",
    "THB", "TJS", "TMT", "TND", "TOP", "TRY", "TTD", "TWD", "TZS",
    "UAH", "UGX", "USD", "USN", "UYI", "UYU", "UYW", "UZS",
    "VES", "VND", "VUV",
    "WST",
    "XAF", "XAG", "XAU", "XBA", "XBB", "XBC", "XBD", "XCD", "XDR", "XOF", "XPD", "XPF", "XPT", "XSU", "XTS", "XUA", "XXX",
    "YER",
    "ZAR", "ZMW", "ZWL"
  )
}
