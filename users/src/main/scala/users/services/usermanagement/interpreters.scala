package users.services.usermanagement

import java.time.OffsetDateTime
import java.util.UUID

import scala.util.Random

import cats.data.EitherT
import cats.implicits.*
import cats.Monad

import scala.concurrent.*

import users.config.*
import users.domain.*
import users.persistence.repositories.*

object Interpreters:

  def default[F[_]: Monad](userRepository: UserRepository[F])(implicit ec: ExecutionContext): Algebra[F] =
    new DefaultInterpreter(userRepository)

  def unreliable(
    underlying: Algebra[Future[*]],
    config: ServicesConfig.UsersConfig
  )(implicit ec: ExecutionContext): Algebra[Future[*]] = new UnreliableInterpreter(underlying, config)

final class DefaultInterpreter[F[_]: Monad] private[usermanagement] (
  userRepository: UserRepository[F]
) extends Algebra[F] {
  import User.*

  def generateId(): F[Id] =
    Id(UUID.randomUUID().toString).pure[F]

  def get(id: Id): F[Error Either User] =
    for
      maybeUser <- userRepository.get(id)
      result = maybeUser.toRight(Error.NotFound)
    yield result

  def signUp(
    userName: UserName,
    emailAddress: EmailAddress,
    password: Option[Password]
  ): F[Error Either User] =
    (for
      maybeUser <- EitherT[F, Error, Option[User]] {
                     userRepository.getByUserName(userName).map(Right.apply)
                   }
      id <- EitherT[F, Error, Id](generateId().map(Right.apply))
      result <- EitherT.fromEither[F] {
                  if (maybeUser.nonEmpty) Left(Error.Exists: Error)
                  else
                    Right(User(id, userName, emailAddress, password, OffsetDateTime.now()))
                }
      _ <- EitherT(save(result))
    yield result).value

  def updateEmail(id: Id, emailAddress: EmailAddress): F[Error Either User] =
    (for
      user <- EitherT(get(id))
      result <- EitherT.fromEither[F] {
                  if (user.isDeleted) Left(Error.Deleted)
                  else Right(user.updateEmailAddress(emailAddress, OffsetDateTime.now()))
                }
      _ <- EitherT(save(result))
    yield result).value

  def updatePassword(id: Id, password: Password): F[Error Either User] =
    (for
      user <- EitherT(get(id))
      result <- EitherT.fromEither[F] {
                  if (user.isDeleted) Left(Error.Deleted)
                  else Right(user.updatePassword(password, OffsetDateTime.now()))
                }
      _ <- EitherT(save(result))
    yield result).value

  def resetPassword(id: Id): F[Error Either User] =
    (for
      user <- EitherT(get(id))
      result <- EitherT.fromEither[F] {
                  if (user.isDeleted) Left(Error.Deleted)
                  else Right(user.resetPassword(OffsetDateTime.now()))
                }
      _ <- EitherT(save(result))
    yield result).value

  def block(id: Id): F[Error Either User] =
    (for
      user <- EitherT(get(id))
      result <- EitherT.fromEither[F] {
                  if (user.isDeleted) Left(Error.Deleted)
                  else if (user.isBlocked) Left(Error.Blocked)
                  else Right(user.block(OffsetDateTime.now))
                }
      _ <- EitherT(save(result))
    yield result).value

  def unblock(id: Id): F[Error Either User] =
    (for
      user <- EitherT(get(id))
      result <- EitherT.fromEither[F] {
                  if (user.isDeleted) Left(Error.Deleted)
                  else if (user.isActive) Left(Error.Active)
                  else Right(user.unblock(OffsetDateTime.now))
                }
      _ <- EitherT(save(result))
    yield result).value

  def delete(id: Id): F[Error Either Done] =
    (for
      user <- EitherT(get(id))
      result <- EitherT.fromEither[F] {
                  if (user.isDeleted) Left(Error.Deleted)
                  else if (user.isActive) Left(Error.Active)
                  else Right(user.delete(OffsetDateTime.now))
                }
      _ <- EitherT(save(result))
    yield Done).value

  def all(): F[Error Either List[User]] =
    for result <- userRepository.all()
    yield result.asRight[Error]

  private def save(user: User): F[Error Either Done] =
    for result <- userRepository.insert(user)
    yield result.asRight[Error]

}

object UnreliableInterpreter {

  private final def nonCompletingFuture[A] = Promise[A]().future

  private def failWithProbability[A](probability: Double)(f: Future[A]) =
    if (Random.nextDouble < probability) Future.failed(new Exception) else f

  private def timeoutWithProbability[A](probability: Double)(f: Future[A]) =
    if (Random.nextDouble < probability) nonCompletingFuture[A] else f

  private def failOrTimeoutWithProbabilities[A](fail: Double, timeout: Double)(f: Future[A]) =
    failWithProbability(fail)(timeoutWithProbability(timeout)(f))
}

final class UnreliableInterpreter private[usermanagement] (
  underlying: Algebra[Future[*]],
  config: ServicesConfig.UsersConfig)
    extends Algebra[Future[*]] {

  import UnreliableInterpreter.*
  import User.*

  def generateId(): Future[Id] =
    underlying.generateId()

  def get(
    id: Id
  ): Future[Error Either User] =
    failOrTimeout(underlying.get(id))

  def signUp(
    userName: UserName,
    emailAddress: EmailAddress,
    password: Option[Password]
  ): Future[Error Either User] =
    failOrTimeout(underlying.signUp(userName, emailAddress, password))

  def updateEmail(
    id: Id,
    emailAddress: EmailAddress
  ): Future[Error Either User] =
    failOrTimeout(underlying.updateEmail(id, emailAddress))

  def updatePassword(
    id: Id,
    password: Password
  ): Future[Error Either User] =
    failOrTimeout(underlying.updatePassword(id, password))

  def resetPassword(
    id: Id
  ): Future[Error Either User] =
    failOrTimeout(underlying.resetPassword(id))

  def block(
    id: Id
  ): Future[Error Either User] =
    failOrTimeout(underlying.block(id))

  def unblock(
    id: Id
  ): Future[Error Either User] =
    failOrTimeout(underlying.unblock(id))

  def delete(
    id: Id
  ): Future[Error Either Done] =
    failOrTimeout(underlying.delete(id))

  def all(): Future[Error Either List[User]] =
    failOrTimeout(underlying.all())

  private def failOrTimeout[A](f: Future[A]): Future[A] =
    failOrTimeoutWithProbabilities(config.failureProbability, config.timeoutProbability)(f)

}
