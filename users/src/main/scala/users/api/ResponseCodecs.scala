package users.api

import users.domain.User

trait ResponseCodecs extends DefaultCodecs {

  implicit val (userMetadataEncoder, userMetadataDecoder) = deriveCodecs[User.Metadata]

  implicit val (userEncoder, userDecoder) = deriveCodecs[User]

}
