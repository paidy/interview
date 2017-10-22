package users.api

import io.circe.{Decoder, Encoder}
import users.domain.{Done, EmailAddress, User, UserName}

trait ResponseCodecs extends DefaultCodecs {

  implicit val (userMetadataEncoder, userMetadataDecoder) = deriveCodecs[User.Metadata]

  implicit val doneEncoder: Encoder[Done] = Encoder.encodeUnit.contramap(_ => ())

  implicit val userDecoder: Decoder[User] = {
    Decoder.forProduct4[User.Id, UserName, EmailAddress, User.Metadata, User](
      "id",
      "userName",
      "emailAddress",
      "metadata"
    ) {
      case (id, userName, emailAddress, metadata) =>
        User(id, userName, emailAddress, None, metadata)
    }
  }

}

trait DefaultResponseCodecs extends ResponseCodecs {

  implicit val userEncoder: Encoder[User] = Encoder.forProduct4("id", "userName", "emailAddress", "metadata")(u =>
    (u.id, u.userName, u.emailAddress, u.metadata))
}

object DefaultResponseCodecs extends DefaultResponseCodecs

trait RestrictedResponseCodecs extends ResponseCodecs {

  implicit val userEncoder: Encoder[User] = Encoder.forProduct3("id", "userName", "emailAddress")(u =>
    (u.id, u.userName, u.emailAddress))

}

object RestrictedResponseCodecs extends RestrictedResponseCodecs
