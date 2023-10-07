package forex.model.domain

import cats.Show
import org.http4s.ParseFailure

import scala.util.{Failure, Try}


object Currency extends Enumeration {

  val AUD = Value("AUD")
  val CAD = Value("CAD")
  val CHF = Value("CHF")
  val EUR = Value("EUR")
  val GBP = Value("GBP")
  val NZD = Value("NZD")
  val JPY = Value("JPY")
  val SGD = Value("SGD")
  val USD = Value("USD")

  implicit val show: Show[Currency.Value] = Show.show(_.toString)

  def fromString(s: String): Try[Currency.Value] = Try(this.withName(s.toUpperCase()))
    .recoverWith(_ => Failure(new ParseFailure(
      s"'$s' is non valid currency ID, use one of: ${this.values.mkString(", ")}", "")))

  lazy val allCurrencyPairs: Set[Rate.Pair] = values.toSet[Currency.Value]
    .flatMap(c1 => values.toSet[Currency.Value]
      .flatMap {
        case c2 if c1 != c2 => Set(Rate.Pair(c1, c2))
        case _ => Set()
      }
    )
}
