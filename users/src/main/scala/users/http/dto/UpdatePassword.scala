package users.http.dto

import users.domain.Password

final case class UpdatePassword(password: Password) derives ConfiguredCodec
