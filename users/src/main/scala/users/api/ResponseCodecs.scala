package users.api

import io.circe.{Decoder, Encoder}
import users.domain.{Done, EmailAddress, User, UserName}
import io.circe.generic.semiauto.deriveEncoder

trait ResponseCodecs extends DefaultCodecs {

  implicit val (userMetadataEncoder, userMetadataDecoder) = deriveCodecs[User.Metadata]

  implicit val userDecoder: Decoder[User] = {
    Decoder.forProduct4[User.Id, UserName, EmailAddress, User.Metadata, User]("id", "userName", "emailAddress", "metadata") {
      case (id, userName, emailAddress, metadata) => User(id, userName, emailAddress, None, metadata)
    }
  }

  implicit val userEncoder = deriveEncoder[User]

  implicit val doneEncoder: Encoder[Done] = Encoder.encodeUnit.contramap(_ => ())

}
