package users.domain

import java.time.OffsetDateTime

import cats.kernel.Eq
import cats.implicits._
import com.softwaremill.quicklens._

final case class User(
    id: User.Id,
    userName: UserName,
    emailAddress: EmailAddress,
    password: Option[Password],
    metadata: User.Metadata
) {
  def status: User.Status =
    User.status(this)

  def isActive: Boolean = status === User.Status.Active
  def isBlocked: Boolean = status === User.Status.Blocked
  def isDeleted: Boolean = status === User.Status.Deleted

  def updateEmailAddress(emailAddress: EmailAddress, at: OffsetDateTime): User =
    User.updateEmailAddress(this, emailAddress, at)

  def updatePassword(password: Password, at: OffsetDateTime): User =
    User.updatePassword(this, password, at)

  def resetPassword(at: OffsetDateTime): User =
    User.resetPassword(this, at)

  def block(at: OffsetDateTime): User =
    User.block(this, at)

  def unblock(at: OffsetDateTime): User =
    User.unblock(this, at)

  def delete(at: OffsetDateTime): User =
    User.delete(this, at)
}

object User {
  def apply(
      id: User.Id,
      userName: UserName,
      emailAddress: EmailAddress,
      password: Option[Password],
      at: OffsetDateTime
  ): User = User(id, userName, emailAddress, password, Metadata(1, at, at, None, None))

  final case class Id(value: String) extends AnyVal

  final case class Metadata(
      version: Int,
      createdAt: OffsetDateTime,
      updatedAt: OffsetDateTime,
      blockedAt: Option[OffsetDateTime],
      deletedAt: Option[OffsetDateTime]
  )

  sealed trait Status
  object Status {
    final case object Active extends Status
    final case object Blocked extends Status
    final case object Deleted extends Status

    implicit val eq: Eq[Status] =
      Eq.fromUniversalEquals
  }

  final def status(user: User): Status =
    if (user.metadata.deletedAt.isDefined) Status.Deleted
    else if (user.metadata.blockedAt.isDefined) Status.Blocked
    else Status.Active

  def updateEmailAddress(user: User, emailAddress: EmailAddress, at: OffsetDateTime): User =
    user
      .modify(_.emailAddress)
      .setTo(emailAddress)
      .modify(_.metadata.updatedAt)
      .setTo(at)
      .modify(_.metadata.version)
      .using(_ + 1)

  def updatePassword(user: User, password: Password, at: OffsetDateTime): User =
    user
      .modify(_.password)
      .setTo(Some(password))
      .modify(_.metadata.updatedAt)
      .setTo(at)
      .modify(_.metadata.version)
      .using(_ + 1)

  def resetPassword(user: User, at: OffsetDateTime): User =
    user
      .modify(_.password)
      .setTo(None)
      .modify(_.metadata.updatedAt)
      .setTo(at)
      .modify(_.metadata.version)
      .using(_ + 1)

  def block(user: User, at: OffsetDateTime): User =
    user
      .modify(_.metadata.blockedAt)
      .setTo(Some(at))
      .modify(_.metadata.updatedAt)
      .setTo(at)
      .modify(_.metadata.version)
      .using(_ + 1)

  def unblock(user: User, at: OffsetDateTime): User =
    user
      .modify(_.metadata.blockedAt)
      .setTo(None)
      .modify(_.metadata.updatedAt)
      .setTo(at)
      .modify(_.metadata.version)
      .using(_ + 1)

  def delete(user: User, at: OffsetDateTime): User =
    user
      .modify(_.metadata.deletedAt)
      .setTo(Some(at))
      .modify(_.metadata.updatedAt)
      .setTo(at)
      .modify(_.metadata.version)
      .using(_ + 1)
}
