package users.domain

import io.circe.*
import io.circe.derivation.*
import io.circe.generic.semiauto.*

object Protocol:

  implicit val idCodec: Codec[User.Id] = Codec.from(
    Decoder.decodeString.map(User.Id.apply),
    Encoder.encodeString.contramap(_.value)
  )

  implicit val userNameCodec: Codec[UserName] = Codec.from(
    Decoder.decodeString.map(UserName.apply),
    Encoder.encodeString.contramap(_.value)
  )

  implicit val emailAddressCodec: Codec[EmailAddress] = Codec.from(
    Decoder.decodeString.map(EmailAddress.apply),
    Encoder.encodeString.contramap(_.value)
  )

  implicit val passwordCodec: Codec[Password] = Codec.from(
    Decoder.decodeString.map(Password.apply),
    Encoder.encodeString.contramap(_.value)
  )
  implicit val metadataCodec: Codec[User.Metadata] = deriveCodec[User.Metadata]

  implicit val userCodec: Codec[User] = deriveCodec[User]
