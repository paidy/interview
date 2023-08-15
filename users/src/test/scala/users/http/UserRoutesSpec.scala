package users.http

import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.scalatest.compatible.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import org.scalatest.EitherValues
import org.scalatest.OptionValues
import org.typelevel.ci.CIString
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.SelfAwareStructuredLogger

import cats.data.OptionT
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import cats.implicits.*
import io.circe.*
import io.circe.literal.*

import users.domain.*
import users.http.dto.*
import users.services.UserManagement
import users.Mock

class UserRoutesSpec
    extends AsyncWordSpecLike
    with AsyncIOSpec
    with Matchers
    with Mock
    with EitherValues
    with OptionValues:

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  "UserRoutes" should {
    "return Forbidden for auth protected urls" in {
      val storage = userManagementMock
      val routes = UserRoutes.make[IO](storage)

      routes.routes
        .run(Request[IO](Method.GET, uri"/me"))
        .assert(_.status shouldBe Forbidden)
    }

    "return BadRequest with error for incorrect json" in {
      val storage = userManagementMock
      val routes = UserRoutes.make[IO](storage)

      routes.routes
        .run(Request[IO](Method.POST, uri"/signup").withEntity(json"""{"test": "json"}"""))
        .assert(_.status shouldBe BadRequest)
    }

    "return BadRequest with error for incorrect email" in {
      val storage = userManagementMock
      val routes = UserRoutes.make[IO](storage)
      val signupForm = SignupForm(
        UserName("test-user"),
        EmailAddress("test-test.com"),
        None
      )

      routes.routes
        .run(Request[IO](Method.POST, uri"/signup").withEntity[IO, SignupForm](signupForm))
        .assert(_.status shouldBe BadRequest)
    }

    "create a user" in {
      val storage = userManagementMock
      val routes = UserRoutes.make[IO](storage)
      val signupForm = SignupForm(
        UserName("test-user"),
        EmailAddress("test@test.com"),
        None
      )

      routes.routes
        .run(Request[IO](Method.POST, uri"/signup").withEntity(signupForm))
        .assertF { r =>
          r.status shouldBe Ok
          val id = User.Id(r.headers.get(CIString("token")).value.head.value)
          r.as[UserInfo].flatMap { u =>
            u.email shouldBe signupForm.emailAddress
            u.username shouldBe signupForm.userName

            storage.get(id).map(_.value.short shouldBe u)
          }
        }
    }

    "update email for a user" in {
      val storage = userManagementMock
      val routes = UserRoutes.make[IO](storage)

      val newEmail = EmailAddress("test.new@test.com")

      (for
        user <- initUser(storage)
        req <- Request[IO](Method.POST, uri"/update-email")
                 .withEntity(UpdateEmail(newEmail))
                 .withHeaders(tokenHeader(user.id))
                 .pure[IO]
        response <- routes.routes.run(req).map(_.as[UserInfo]).value
        userInfo <- response.value
      yield userInfo).asserting { r =>
        r.email shouldBe newEmail
      }
    }

    "update password for a user" in {
      val storage = userManagementMock
      val routes = UserRoutes.make[IO](storage)

      val newPassword = Password("123")

      (for
        user <- initUser(storage)
        req <- Request[IO](Method.POST, uri"/update-password")
                 .withEntity(UpdatePassword(newPassword))
                 .withHeaders(tokenHeader(user.id))
                 .pure[IO]
        response <- routes.routes.run(req).value
        updatedUser <- storage.get(user.id).map(_.value)
      yield (response, updatedUser)).asserting { case (r, updatedUser) =>
        r.value.status shouldBe Ok
        updatedUser.password.value shouldBe newPassword
      }
    }

    "reset password for a user" in {
      val storage = userManagementMock
      val routes = UserRoutes.make[IO](storage)

      (for
        user <- initUser(storage)
        _ <- storage.updatePassword(user.id, Password("123")).map(_.value)
        req <- Request[IO](Method.POST, uri"/reset-password")
                 .withHeaders(tokenHeader(user.id))
                 .pure[IO]
        response <- routes.routes.run(req).value
        updatedUser <- storage.get(user.id).map(_.value)
      yield (response, updatedUser)).asserting { case (r, updatedUser) =>
        r.value.status shouldBe Ok
        updatedUser.password shouldBe Symbol("Empty")
      }
    }
  }

  private def initUser(service: UserManagement[IO]): IO[User] =
    service.signUp(UserName("test-user"), EmailAddress("test@test.com"), None).map(_.value)

  private def tokenHeader(id: User.Id): Header.Raw = Header.Raw(CIString("token"), id.value)

  extension (response: OptionT[IO, Response[IO]])

    def assert(check: Response[IO] => Assertion): IO[Assertion] =
      response.value.asserting(r => check(r.value))

    def assertF(check: Response[IO] => IO[Assertion]): IO[Assertion] =
      response.value.flatMap {
        case Some(r) => check(r)
        case None => IO.pure(fail("No response"))
      }
