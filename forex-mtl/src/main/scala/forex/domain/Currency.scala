package forex.domain

import cats.Show
import cats.data.ValidatedNel
import cats.implicits._
import org.http4s.ParseFailure

sealed trait Currency

object Currency {
  case object AUD extends Currency
  case object CAD extends Currency
  case object CHF extends Currency
  case object EUR extends Currency
  case object GBP extends Currency
  case object NZD extends Currency
  case object JPY extends Currency
  case object SGD extends Currency
  case object USD extends Currency

  implicit val show: Show[Currency] = Show.show {
    case AUD => "AUD"
    case CAD => "CAD"
    case CHF => "CHF"
    case EUR => "EUR"
    case GBP => "GBP"
    case NZD => "NZD"
    case JPY => "JPY"
    case SGD => "SGD"
    case USD => "USD"
  }

  def fromString(s: String): ValidatedNel[ParseFailure, Currency] = s.toUpperCase match {
    case "AUD" => AUD.validNel[ParseFailure]
    case "CAD" => CAD.validNel[ParseFailure]
    case "CHF" => CHF.validNel[ParseFailure]
    case "EUR" => EUR.validNel[ParseFailure]
    case "GBP" => GBP.validNel[ParseFailure]
    case "NZD" => NZD.validNel[ParseFailure]
    case "JPY" => JPY.validNel[ParseFailure]
    case "SGD" => SGD.validNel[ParseFailure]
    case "USD" => USD.validNel[ParseFailure]
    case str   => ParseFailure(s"Can not parse currency $str", "").invalidNel[Currency]
  }

  val values: Seq[Currency] =
    Seq(AUD, CAD, CHF, EUR, GBP, NZD, JPY, SGD, USD)
}
