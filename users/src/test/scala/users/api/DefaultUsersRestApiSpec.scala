package users.api

import akka.http.scaladsl.model.StatusCodes
import org.scalatest.{Matchers, WordSpec}
import users.domain._

class DefaultUsersRestApiSpec extends WordSpec with Matchers with UserRestApiSpec {

  "Rest api" should {

    "create user record for PUT /admin/user request" in withUser("john") { user =>

      user shouldBe User(
        user.id,
        UserName("john"),
        EmailAddress("john@fake.com"),
        None,
        user.metadata
      )
    }

    "get user by id for GET /admin/user/{id} request" in withUser("Jim") { user =>

      Get(s"/admin/user/${user.id}") ~> usersRestApi.routes ~> check {
        responseAs[User] shouldBe user
      }
    }

    "update user's email by id for POST /admin/user/{id}/email request" in withUser("jack") { user =>

      val newEmail = EmailAddress("new@email.com")
      val request = UpdateEmailRequest(newEmail)

      Post(s"/admin/user/${user.id}/email", marshal(request)) ~> usersRestApi.routes ~> check {
        val newUser = responseAs[User]
        newUser.emailAddress shouldBe newEmail
        newUser.metadata.version shouldBe user.metadata.version + 1
      }
    }

    "update user's password by id for POST /admin/user/{id}/password request" in withUser("jane") { user =>

      val newEmail = Password("654321")
      val request = UpdatePasswordRequest(newEmail)

      Post(s"/admin/user/${user.id}/password", marshal(request)) ~> usersRestApi.routes ~> check {
        responseAs[User].metadata.version shouldBe user.metadata.version + 1
      }
    }

    "reset user's password by id for DELETE /admin/user/{id}/password request" in withUser("johny") { user =>

      Delete(s"/admin/user/${user.id}/password") ~> usersRestApi.routes ~> check {
        val newUser = responseAs[User]
        newUser.metadata.version shouldBe user.metadata.version + 1
      }
    }

    "block user by id for POST /admin/user/{id}/block request" in withUser("jake") { user =>

      Post(s"/admin/user/${user.id}/block") ~> usersRestApi.routes ~> check {
        val newUser = responseAs[User]
        newUser.metadata.blockedAt shouldBe 'defined
        newUser.metadata.version shouldBe user.metadata.version + 1
      }
    }

    "unblock user by id for DELETE /admin/user/{id}/block request" in withUser("jimbo") { user =>

      Post(s"/admin/user/${user.id}/block") ~> usersRestApi.routes ~> runRoute
      Delete(s"/admin/user/${user.id}/block") ~> usersRestApi.routes ~> check {
        val newUser = responseAs[User]
        newUser.metadata.blockedAt shouldBe 'empty
        newUser.metadata.version shouldBe user.metadata.version + 2
      }
    }

    "delete user by id for DELETE /admin/user/{id} request" in withUser("jerome") { user =>

      Post(s"/admin/user/${user.id}/block") ~> usersRestApi.routes ~> runRoute
      Delete(s"/admin/user/${user.id}") ~> usersRestApi.routes ~> check {
        response.status.intValue() shouldBe 200
      }
      Get(s"/admin/user/${user.id}") ~> usersRestApi.routes ~> check {
        responseAs[User].metadata.deletedAt shouldBe 'defined
      }
    }

    "get all users by id for GET /admin/users request" in withUser("jeckyl") { user =>

      Get(s"/admin/users") ~> usersRestApi.routes ~> check {
        responseAs[List[User]] should contain(
          user.copy(password = None)
        )
      }
    }
  }

  "Restricted rest api" should {

    "not allow to block user by id for POST /user/{id}/block request" in withUser("jake") { user =>

      Post(s"/user/${user.id}/block") ~> usersRestApi.routes ~> check {
        response.status.intValue() shouldBe StatusCodes.Unauthorized.intValue
      }
    }

    "not allow to unblock user by id for DELETE /user/{id}/block request" in withUser("jimbo") { user =>

      Post(s"/admin/user/${user.id}/block") ~> usersRestApi.routes ~> runRoute
      Delete(s"/user/${user.id}/block") ~> usersRestApi.routes ~> check {
        response.status.intValue() shouldBe StatusCodes.Unauthorized.intValue
      }
    }

    "not allow to delete user by id for DELETE /user/{id} request" in withUser("jerome") { user =>

      Post(s"/admin/user/${user.id}/block") ~> usersRestApi.routes ~> runRoute
      Delete(s"/admin/user/${user.id}") ~> usersRestApi.routes ~> check {
        response.status.intValue() shouldBe 200
      }
      Get(s"/user/${user.id}") ~> usersRestApi.routes ~> check {
        response.status.intValue() shouldBe 404
      }
    }

    "get all active users by id for GET /users request" in withUser("jeckyl") { user =>

      Post(s"/admin/user/${user.id}/block") ~> usersRestApi.routes ~> runRoute
      Delete(s"/admin/user/${user.id}") ~> usersRestApi.routes ~> check {
        response.status.intValue() shouldBe 200
      }
      Get(s"/users") ~> usersRestApi.routes ~> check {
        responseAs[List[UserResponse]] should not contain UserResponse(
          user.id,
          user.userName,
          user.emailAddress
        )
      }
    }
  }
}
