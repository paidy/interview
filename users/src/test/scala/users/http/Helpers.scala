package users.http

import org.http4s.*
import org.scalatest.EitherValues
import org.scalatest.OptionValues
import org.scalatest.Suite
import org.typelevel.ci.CIString
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.SelfAwareStructuredLogger

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO

import users.domain.*
import users.services.UserManagement

trait Helpers:
  self: AsyncIOSpec with EitherValues with OptionValues with Suite =>

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def userRoutes(storage: UserManagement[IO]) =
    UserRoutes.make[IO](storage).routes.orNotFound

  def adminRoutes(storage: UserManagement[IO]) =
    AdminRoutes.make[IO](storage).routes.orNotFound

  protected def initUser(service: UserManagement[IO]): IO[User] =
    service.signUp(UserName("test-user"), EmailAddress("test@test.com"), None).map(_.value)

  protected def tokenHeader(id: User.Id): Header.Raw = Header.Raw(CIString("token"), id.value)
