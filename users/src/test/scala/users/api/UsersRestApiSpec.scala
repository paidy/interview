package users.api

import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.scalatest.{Matchers, WordSpec}
import users.config.{ExecutorsConfig, ServicesConfig}
import users.domain._
import users.main.{Apis, Executors, Repositories, Services}

class UsersRestApiSpec extends WordSpec with Matchers with ScalatestRouteTest
  with FailFastCirceSupport with RequestCodecs with ResponseCodecs {

  case class TestConfig(
    services: ServicesConfig,
    executors: ExecutorsConfig
  )

  val config = TestConfig(
    executors = ExecutorsConfig(
      services = ExecutorsConfig.ServicesConfig(
        parallellism = 1
      )
    ),
    services = ServicesConfig(
      users = ServicesConfig.UsersConfig(
        failureProbability = 0.0,
        timeoutProbability = 0.0
      )
    )
  )

  val usersRestApi: HttpApi = (for {
    repositories <- Repositories.reader.local[TestConfig](_ => ())
    executors <- Executors.reader.local[TestConfig](_.executors)
    services = Services.reader.local[TestConfig] {
      c => (c.services, executors, repositories)
    }
    apis <- services andThen Apis.reader
  } yield apis).run(config).userRestApi


  private def withUser[T](body: User => T): T = {
    val request = SignUpRequest(
      UserName("John"),
      EmailAddress("fake@email"),
      Some(Password("1234567"))
    )

    Post("/user", marshal(request)) ~> usersRestApi.routes ~> check {
      body(responseAs[User])
    }
  }

  "Rest api" should {

    "create user record for PUT request" in withUser { user =>

      user shouldBe User(
        user.id,
        UserName("John"),
        EmailAddress("fake@email"),
        None,
        user.metadata
      )
    }

    "get user by id for GET request" in withUser { user =>

      Get(s"/user/${user.id}") ~> usersRestApi.routes ~> check {
        responseAs[User] shouldBe user
      }
    }

    "update user's email by id for POST request" in withUser { user =>

      val newEmail = EmailAddress("new@email.com")
      val request = UpdateEmailRequest(newEmail)

      Post(s"/user/${user.id}/email", marshal(request)) ~> usersRestApi.routes ~> check {
        val newUser = responseAs[User]
        newUser.emailAddress shouldBe newEmail
        newUser.metadata.version shouldBe user.metadata.version + 1
      }
    }

    "update user's password by id for POST request" in withUser { user =>

      val newEmail = EmailAddress("new@email.com")
      val request = UpdateEmailRequest(newEmail)

      Post(s"/user/${user.id}/password", marshal(request)) ~> usersRestApi.routes ~> check {
        responseAs[User].metadata.version shouldBe user.metadata.version + 1
      }
    }

    "reset user's password by id for DELETE request" in withUser { user =>

      Delete(s"/user/${user.id}/password") ~> usersRestApi.routes ~> check {
        val newUser = responseAs[User]
        newUser.metadata.version shouldBe user.metadata.version + 1
      }
    }

    "block user by id for POST request" in withUser { user =>

      Post(s"/user/${user.id}/block") ~> usersRestApi.routes ~> check {
        val newUser = responseAs[User]
        newUser.metadata.blockedAt shouldBe 'defined
        newUser.metadata.version shouldBe user.metadata.version + 1
      }
    }

    "unblock user by id for DELETE request" in withUser { user =>

      Delete(s"/user/${user.id}/block") ~> usersRestApi.routes ~> check {
        val newUser = responseAs[User]
        newUser.metadata.blockedAt shouldBe 'empty
        newUser.metadata.version shouldBe user.metadata.version + 1
      }
    }

    "delete user by id for DELETE request" in withUser { user =>

      Delete(s"/user/${user.id}") ~> usersRestApi.routes ~> check {
        Get(s"/user/${user.id}") ~> usersRestApi.routes ~> check {
          response.status.intValue() shouldBe 404
        }
      }
    }

    "get all user by id for GET request" in withUser { user =>

      Get(s"/users") ~> usersRestApi.routes ~> check {
        responseAs[List[User]].size shouldBe 7
      }
    }
  }
}
