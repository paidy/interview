package users.domain

import cats.kernel.Eq

final case class UserName(value: String) extends AnyVal

object UserName {
  implicit val eq: Eq[UserName] =
    Eq.fromUniversalEquals
}
