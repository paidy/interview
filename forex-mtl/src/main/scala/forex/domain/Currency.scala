package forex.domain

import cats.Show

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

  def fromString(s: String): Currency = s.toUpperCase match {
    case "AUD" => AUD
    case "CAD" => CAD
    case "CHF" => CHF
    case "EUR" => EUR
    case "GBP" => GBP
    case "NZD" => NZD
    case "JPY" => JPY
    case "SGD" => SGD
    case "USD" => USD
  }

  import scala.reflect.runtime.universe._

  def findSubclasses[T: TypeTag]: List[String] = {
    val _ = runtimeMirror(getClass.getClassLoader)
    val parentType = typeOf[T]

    val subclasses = for {
      sym <- parentType.typeSymbol.asClass.knownDirectSubclasses
      if sym.isClass
    } yield sym.name.toString

    subclasses.toList
  }

  val allCurrencies = findSubclasses[Currency]
  val allPairs = for {
    i <- allCurrencies
    j <- allCurrencies
    if i != j
  } yield (i,j)
}
