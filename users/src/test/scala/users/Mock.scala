package users

import java.time.OffsetDateTime

import scala.collection.concurrent.TrieMap

import cats.effect.IO
import cats.implicits.*

import users.domain.Done
import users.domain.EmailAddress
import users.domain.Password
import users.domain.User
import users.domain.User.Id
import users.domain.UserName
import users.services.usermanagement
import users.services.UserManagement

trait Mock:

  trait HasAdminHelper:
    def genAdmin(): IO[User]

  def userManagementMock: UserManagement[IO] with HasAdminHelper = new UserManagement[IO] with HasAdminHelper:

    private val storage: TrieMap[Id, User] = TrieMap.empty

    def genAdmin(): IO[User] =
      User(
        User.Id.gen,
        userName = UserName("admin"),
        emailAddress = EmailAddress("admin@test.com"),
        None,
        User.Metadata(1, OffsetDateTime.now, OffsetDateTime.now, None, None),
        isAdmin = true
      ).pure[IO].map { u =>
        storage.put(u.id, u)
        u
      }

    override def all(): IO[Either[usermanagement.Error, List[User]]] =
      storage.values.toList.asRight[usermanagement.Error].pure[IO]

    override def block(id: Id): IO[Either[usermanagement.Error, User]] =
      updateAndReturn(id)(_.block(OffsetDateTime.now))

    override def delete(id: Id): IO[Either[usermanagement.Error, Done]] =
      Either.fromOption(storage.remove(id).map(_ => Done), usermanagement.Error.NotFound).pure[IO]

    override def generateId(): IO[Id] = Id.gen.pure[IO]

    override def get(id: Id): IO[Either[usermanagement.Error, User]] =
      Either.fromOption(storage.get(id), usermanagement.Error.NotFound).pure[IO]

    override def resetPassword(id: Id): IO[Either[usermanagement.Error, User]] =
      updateAndReturn(id)(_.resetPassword(OffsetDateTime.now))

    override def signUp(
      userName: UserName,
      emailAddress: EmailAddress,
      password: Option[Password]): IO[Either[usermanagement.Error, User]] =
      (storage.find(_._2.userName == userName) match
        case Some(u) => usermanagement.Error.Exists.asLeft[User]
        case None =>
          val id = Id.gen
          val u = User(
            id,
            userName,
            emailAddress,
            password,
            OffsetDateTime.now()
          )
          storage.put(id, u)
          u.asRight[usermanagement.Error]
      ).pure[IO]

    override def unblock(id: Id): IO[Either[usermanagement.Error, User]] =
      updateAndReturn(id)(_.unblock(OffsetDateTime.now))

    override def updateEmail(id: Id, emailAddress: EmailAddress): IO[Either[usermanagement.Error, User]] =
      (storage.get(id) match {
        case Some(u) =>
          storage.find(_._2.emailAddress == emailAddress) match {
            case Some(active) if active._1 != id => usermanagement.Error.Exists.asLeft[User]
            case _ =>
              storage.update(id, u.updateEmailAddress(emailAddress, OffsetDateTime.now()))
              Either.fromOption(
                storage.get(id),
                usermanagement.Error.NotFound
              )
          }
        case None => usermanagement.Error.NotFound.asLeft[User]
      }).pure[IO]

    override def updatePassword(id: Id, password: Password): IO[Either[usermanagement.Error, User]] =
      updateAndReturn(id)(_.updatePassword(password, OffsetDateTime.now()))

    private def updateAndReturn(id: Id)(updF: User => User): IO[Either[usermanagement.Error, User]] =
      Either
        .fromOption(
          for
            user <- storage.get(id)
            _ = storage.update(id, updF(user))
            userUpd <- storage.get(id)
          yield userUpd,
          usermanagement.Error.NotFound
        )
        .pure[IO]
