package users

import shapeless.tag.@@
import shapeless.tag

package object domain {
  type Done = Done.type

  trait EmailAddressTag
  type EmailAddress = String @@ EmailAddressTag
  def EmailAddress(in: String): EmailAddress = tag[EmailAddressTag][String](in)

  trait UserNameTag
  type UserName = String @@ UserNameTag
  def UserName(in: String): UserName = tag[UserNameTag][String](in)

  trait PasswordTag
  type Password = String @@ PasswordTag
  def Password(in: String): Password = tag[PasswordTag][String](in)

  def UserId(in: String): User.Id = tag[User][String](in)
}
