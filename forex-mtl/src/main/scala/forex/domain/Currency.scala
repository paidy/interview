package forex.domain

import enumeratum.values.{ Circe, StringEnum, StringEnumEntry }
import io.circe.Codec

import scala.collection.immutable

sealed abstract class Currency(val value: String) extends StringEnumEntry

object Currency extends StringEnum[Currency] {
  case object AUD extends Currency("AUD")
  case object CAD extends Currency("CAD")
  case object CHF extends Currency("CHF")
  case object EUR extends Currency("EUR")
  case object GBP extends Currency("GBP")
  case object NZD extends Currency("NZD")
  case object JPY extends Currency("JPY")
  case object SGD extends Currency("SGD")
  case object USD extends Currency("USD")

  override def values: immutable.IndexedSeq[Currency] = findValues

  implicit def stringEnumCodec[T <: StringEnumEntry](implicit enum: StringEnum[T]): Codec[T] =
    Codec.from(Circe.decoder[String, T](enum), Circe.encoder[String, T](enum))

  val permutationsPairsString: Seq[(String, String)] = for {
    curA <- values
    curB <- values.filterNot(_ == curA)
  } yield ("pair", s"${curA.toString}${curB.toString}")

}
