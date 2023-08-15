package users.http.dto

import users.domain.*

final case class UserInfo(username: UserName, email: EmailAddress) derives ConfiguredCodec

extension (u: User)
  def short: UserInfo = UserInfo(u.userName, u.emailAddress)
  def withoutPassword: User = u.copy(password = None)
