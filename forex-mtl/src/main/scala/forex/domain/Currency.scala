package forex.domain
import cats.Show
import io.circe.{Decoder, Encoder}

sealed trait Currency

object Currency {

  implicit val decodeCurrency: Decoder[Currency] = Decoder.decodeString.emap { str =>
    fromStringOption(str) match {
      case Some(currency) => Right(currency)
      case None           => Left(s"Invalid currency: $str")
    }
  }

  implicit val encodeCurrency: Encoder[Currency] = Encoder.encodeString.contramap[Currency](_.toString)

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
  def fromStringOption(s: String): Option[Currency] = s.toUpperCase match {
    case "AUD" => Some(AUD)
    case "CAD" => Some(CAD)
    case "CHF" => Some(CHF)
    case "EUR" => Some(EUR)
    case "GBP" => Some(GBP)
    case "NZD" => Some(NZD)
    case "JPY" => Some(JPY)
    case "SGD" => Some(SGD)
    case "USD" => Some(USD)
    case _     => None
  }

}
