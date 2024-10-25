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

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.IO
import cats.implicits.*

import users.domain.*
import users.http.dto.*
import users.services.usermanagement
import users.Mock

class AdminRoutesSpec
    extends AsyncWordSpecLike
    with AsyncIOSpec
    with Matchers
    with Mock
    with EitherValues
    with OptionValues
    with Helpers:

  "AdminRoutes" should {
    "forbid access to non-admin" in {
      val storage = userManagementMock

      (for {
        user <- initUser(storage)
        request <-
          Request(Method.GET, uri"/admin".withQueryParam("id", user.id.value))
            .withHeaders(tokenHeader(user.id))
            .pure[IO]
        response <- adminRoutes(storage).run(request)
      } yield response).asserting(_.status shouldBe Forbidden)
    }

    "return any user" in {
      val storage = userManagementMock
      val routes = adminRoutes(storage)

      (for
        admin <- storage.genAdmin()
        user <- initUser(storage)

        adminRequest <-
          Request(Method.GET, uri"/admin".withQueryParam("id", admin.id.value))
            .withHeaders(tokenHeader(admin.id))
            .pure[IO]
        userRequest <-
          Request(Method.GET, uri"/admin".withQueryParam("id", user.id.value))
            .withHeaders(tokenHeader(admin.id))
            .pure[IO]

        adminResponse <- routes.run(adminRequest).flatMap(_.as[User])
        userResponse <- routes.run(userRequest).flatMap(_.as[User])
      yield (admin, user, adminResponse, userResponse)).asserting {
        case (admin, user, adminUserResponse, userResponse) =>
          admin.copy(password = None) shouldBe adminUserResponse
          user.copy(password = None) shouldBe userResponse
      }
    }

    "update email" in {
      val storage = userManagementMock
      val routes = adminRoutes(storage)
      val newEmail = EmailAddress("test.new@test.com")

      (for
        admin <- storage.genAdmin()
        user <- initUser(storage)

        userRequest <-
          Request(Method.POST, uri"/admin/update-email".withQueryParam("id", user.id.value))
            .withHeaders(tokenHeader(admin.id))
            .withEntity(UpdateEmail(newEmail))
            .pure[IO]

        userResponse <- routes.run(userRequest).flatMap(_.as[User])
        persistedUser <- storage.get(user.id)
      yield (userResponse, persistedUser)).asserting { case (userResponse, persistedUser) =>
        userResponse.emailAddress shouldBe newEmail
        persistedUser.value.emailAddress shouldBe newEmail
      }
    }

    "reset password" in {
      val storage = userManagementMock
      val routes = adminRoutes(storage)

      (for
        admin <- storage.genAdmin()
        user <- initUser(storage).flatMap(u => storage.updatePassword(u.id, Password("123")).map(_.value))

        userRequest <-
          Request(Method.POST, uri"/admin/reset-password".withQueryParam("id", user.id.value))
            .withHeaders(tokenHeader(admin.id))
            .pure[IO]

        _ <- routes.run(userRequest).flatMap(_.as[User])
        persistedUser <- storage.get(user.id)
      yield persistedUser).asserting { case persistedUser =>
        persistedUser.value.password shouldBe Symbol("Empty")
      }
    }

    "fail to block itself" in {
      val storage = userManagementMock
      (for
        admin <- storage.genAdmin()

        userRequest <-
          Request(Method.POST, uri"/admin/block".withQueryParam("id", admin.id.value))
            .withHeaders(tokenHeader(admin.id))
            .pure[IO]

        response <- adminRoutes(storage).run(userRequest)
      yield response).asserting { case response =>
        response.status shouldBe BadRequest
      }
    }

    "block user" in {
      val storage = userManagementMock
      val routes = adminRoutes(storage)

      (for
        admin <- storage.genAdmin()
        user <- initUser(storage)

        userRequest <-
          Request(Method.POST, uri"/admin/block".withQueryParam("id", user.id.value))
            .withHeaders(tokenHeader(admin.id))
            .pure[IO]

        _ <- routes.run(userRequest).flatMap(_.as[User])
        persistedUser <- storage.get(user.id)
      yield persistedUser).asserting { case persistedUser =>
        persistedUser.value.isBlocked shouldBe true
      }
    }

    "unblock user" in {
      val storage = userManagementMock
      val routes = adminRoutes(storage)

      (for
        admin <- storage.genAdmin()
        user <- initUser(storage).flatMap(u => storage.block(u.id).map(_.value))

        userRequest <-
          Request(Method.POST, uri"/admin/unblock".withQueryParam("id", user.id.value))
            .withHeaders(tokenHeader(admin.id))
            .pure[IO]

        _ <- routes.run(userRequest).flatMap(_.as[User])
        persistedUser <- storage.get(user.id)
      yield persistedUser).asserting { case persistedUser =>
        persistedUser.value.isBlocked shouldBe false
      }
    }

    "not delete itself" in {
      val storage = userManagementMock

      (for
        admin <- storage.genAdmin()
        userRequest <-
          Request(Method.DELETE, uri"/admin/delete".withQueryParam("id", admin.id.value))
            .withHeaders(tokenHeader(admin.id))
            .pure[IO]

        response <- adminRoutes(storage).run(userRequest)
      yield response).asserting { case response =>
        response.status shouldBe BadRequest
      }
    }

    "delete user" in {
      val storage = userManagementMock
      val routes = adminRoutes(storage)

      (for
        admin <- storage.genAdmin()
        user <- initUser(storage)

        userRequest <-
          Request(Method.DELETE, uri"/admin/delete".withQueryParam("id", user.id.value))
            .withHeaders(tokenHeader(admin.id))
            .pure[IO]

        response <- routes.run(userRequest)
        persistedUser <- storage.get(user.id)
      yield (response, persistedUser)).asserting { case (response, persistedUser) =>
        response.status shouldBe Ok
        persistedUser shouldBe Symbol("Left")
        persistedUser.left.value shouldBe usermanagement.Error.NotFound
      }
    }

    "return all users" in {
      val storage = userManagementMock
      val routes = adminRoutes(storage)

      (for
        admin <- storage.genAdmin()
        user <- initUser(storage)

        userRequest <-
          Request(Method.GET, uri"/admin/all")
            .withHeaders(tokenHeader(admin.id))
            .pure[IO]

        users <- routes.run(userRequest).flatMap(_.as[List[User]])
        persistedUsers <- storage.all()
      yield (users, persistedUsers)).asserting { case (users, persistedUsers) =>
        users should contain theSameElementsAs persistedUsers.value.map(_.withoutPassword)
      }
    }
  }
