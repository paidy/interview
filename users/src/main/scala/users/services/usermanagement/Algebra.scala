package users.services.usermanagement

import users.domain._

trait Algebra[F[_]] {
  import User._

  def generateId(): F[Id]

  def get(
      id: Id
  ): F[Error Either User]

  def signUp(
      userName: UserName,
      emailAddress: EmailAddress,
      password: Option[Password]
  ): F[Error Either User]

  def updateEmail(
      id: Id,
      emailAddress: EmailAddress
  ): F[Error Either User]

  def updatePassword(
      id: Id,
      password: Password
  ): F[Error Either User]

  def resetPassword(
      id: Id
  ): F[Error Either User]

  def block(
      id: Id
  ): F[Error Either User]

  def unblock(
      id: Id
  ): F[Error Either User]

  def delete(
      id: Id
  ): F[Error Either Done]

  def all(): F[Error Either List[User]]

}
