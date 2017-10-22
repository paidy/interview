package users.api

trait RequestCodecs extends DefaultCodecs {

  implicit val (signUpRequestEncoder, signUpRequestDecoder) = deriveCodecs[SignUpRequest]

  implicit val (updateEmailRequestEncoder, updateEmailRequestDecoder) = deriveCodecs[UpdateEmailRequest]

  implicit val (updatePasswordRequestEncoder, updatePasswordRequestDecoder) = deriveCodecs[UpdatePasswordRequest]

}

object RequestCodecs extends RequestCodecs