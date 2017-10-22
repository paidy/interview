package users.api

import io.circe.{Decoder, Encoder}
import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.encoding.DerivedObjectEncoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.java8.time.TimeInstances
import shapeless.Lazy
import shapeless.tag.@@
import shapeless.tag

trait DefaultCodecs extends TimeInstances {

  /** convience method, both encoder & coder in one call */
  protected def deriveCodecs[T](
    implicit
    encode: Lazy[DerivedObjectEncoder[T]],
    decode: Lazy[DerivedDecoder[T]]
  ): (Encoder[T], Decoder[T]) = deriveEncoder[T] -> deriveDecoder[T]

  implicit def `@@StringDecoder`[T]: Decoder[String @@ T] = {
    Decoder.decodeString.map(x => tag[T](x))
  }

  implicit def `@@StringEncoder`[T <: String]: Encoder[T] = {
    Encoder.encodeString.contramap(identity)
  }
}
