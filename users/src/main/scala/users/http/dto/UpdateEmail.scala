package users.http.dto

import users.domain.EmailAddress

final case class UpdateEmail(
  emailAddress: EmailAddress
) derives ConfiguredCodec
