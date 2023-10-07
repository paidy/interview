package forex.model.http

import cats.effect.Concurrent
import io.circe.generic.extras.decoding.{EnumerationDecoder, UnwrappedDecoder}
import io.circe.generic.extras.encoding.{EnumerationEncoder, UnwrappedEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe._


object Marshalling {

  implicit def valueClassEncoder[A: UnwrappedEncoder]: Encoder[A] = implicitly

  implicit def valueClassDecoder[A: UnwrappedDecoder]: Decoder[A] = implicitly

  implicit def enumEncoder[A: EnumerationEncoder]: Encoder[A] = implicitly

  implicit def enumDecoder[A: EnumerationDecoder]: Decoder[A] = implicitly

  implicit def jsonDecoder[A: Decoder, F[_] : Concurrent]: EntityDecoder[F, A] = jsonOf[F, A]

  implicit def jsonEncoder[A: Encoder, F[_]]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
}
