package users.api

import users.domain.{EmailAddress, Password, UserName}

sealed trait Request

case class SignUpRequest(
  userName: UserName,
  emailAddress: EmailAddress,
  password: Option[Password]
) extends Request

case class UpdateEmailRequest(
  email: EmailAddress
) extends Request

case class UpdatePasswordRequest(
  password: Password
) extends Request