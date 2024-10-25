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

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import cats.implicits.*
import io.circe.*
import io.circe.literal.*

import users.domain.*
import users.http.dto.*
import users.Mock

class UserRoutesSpec
    extends AsyncWordSpecLike
    with AsyncIOSpec
    with Matchers
    with Mock
    with EitherValues
    with OptionValues
    with Helpers:

  "UserRoutes" should {
    "return Forbidden for auth protected urls" in {
      userRoutes(userManagementMock)
        .run(Request[IO](Method.GET, uri"/me"))
        .asserting(_.status shouldBe Forbidden)
    }

    "return BadRequest with error for incorrect json" in {
      userRoutes(userManagementMock)
        .run(Request[IO](Method.POST, uri"/signup").withEntity(json"""{"test": "json"}"""))
        .asserting(_.status shouldBe BadRequest)
    }

    "return BadRequest with error for incorrect email" in {
      val signupForm = SignupForm(
        UserName("test-user"),
        EmailAddress("test-test.com"),
        None
      )

      userRoutes(userManagementMock)
        .run(Request[IO](Method.POST, uri"/signup").withEntity[IO, SignupForm](signupForm))
        .asserting(_.status shouldBe BadRequest)
    }

    "create a user" in {
      val storage = userManagementMock
      val signupForm = SignupForm(
        UserName("test-user"),
        EmailAddress("test@test.com"),
        None
      )

      (for
        response <- userRoutes(storage).run(Request[IO](Method.POST, uri"/signup").withEntity(signupForm))
        id = User.Id(response.headers.get(CIString("token")).value.head.value)
        persistedUser <- storage.get(id)
        userResponse <- response.as[UserInfo]
      yield (persistedUser, userResponse)).asserting { case (persistedUser, userResponse) =>
        userResponse.email shouldBe signupForm.emailAddress
        userResponse.username shouldBe signupForm.userName
        persistedUser.value.short shouldBe userResponse
      }
    }

    "update email for a user" in {
      val storage = userManagementMock

      val newEmail = EmailAddress("test.new@test.com")

      (for
        user <- initUser(storage)
        req <- Request[IO](Method.POST, uri"/update-email")
                 .withEntity(UpdateEmail(newEmail))
                 .withHeaders(tokenHeader(user.id))
                 .pure[IO]
        userInfo <- userRoutes(storage).run(req).flatMap(_.as[UserInfo])
      yield userInfo).asserting { r =>
        r.email shouldBe newEmail
      }
    }

    "update password for a user" in {
      val storage = userManagementMock

      val newPassword = Password("123")

      (for
        user <- initUser(storage)
        req <- Request[IO](Method.POST, uri"/update-password")
                 .withEntity(UpdatePassword(newPassword))
                 .withHeaders(tokenHeader(user.id))
                 .pure[IO]
        response <- userRoutes(storage).run(req)
        updatedUser <- storage.get(user.id).map(_.value)
      yield (response, updatedUser)).asserting { case (r, updatedUser) =>
        r.status shouldBe Ok
        updatedUser.password.value shouldBe newPassword
      }
    }

    "reset password for a user" in {
      val storage = userManagementMock

      (for
        user <- initUser(storage)
        _ <- storage.updatePassword(user.id, Password("123")).map(_.value)
        req <- Request[IO](Method.POST, uri"/reset-password")
                 .withHeaders(tokenHeader(user.id))
                 .pure[IO]
        response <- userRoutes(storage).run(req)
        updatedUser <- storage.get(user.id).map(_.value)
      yield (response, updatedUser)).asserting { case (r, updatedUser) =>
        r.status shouldBe Ok
        updatedUser.password shouldBe Symbol("Empty")
      }
    }
  }
