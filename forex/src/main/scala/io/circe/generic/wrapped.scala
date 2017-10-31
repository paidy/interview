package io.circe.generic.extras

import io.circe._
import io.circe.generic.extras.decoding.UnwrappedDecoder
import io.circe.generic.extras.encoding.UnwrappedEncoder
import shapeless._

/**
  * Temporary back port of semi-automatic derivation for valueclasses,
  * which will be part of the next circe release (0.9.0)
  */
package object wrapped {

  /**
    * Derive a decoder for a value class.
    */
  def deriveUnwrappedDecoder[A](implicit decode: Lazy[UnwrappedDecoder[A]]): Decoder[A] = decode.value

  /**
    * Derive an encoder for a value class.
    */
  def deriveUnwrappedEncoder[A](implicit encode: Lazy[UnwrappedEncoder[A]]): Encoder[A] = encode.value

}
