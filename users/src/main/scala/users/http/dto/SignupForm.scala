package users.http.dto

import users.domain.*

case class SignupForm(
  userName: UserName,
  emailAddress: EmailAddress,
  password: Option[Password]
) derives ConfiguredCodec
