package users.api

import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import users.config.{ExecutorsConfig, ServicesConfig}
import users.domain._
import users.main.{Apis, Executors, Repositories, Services}

class UsersRestApiSpec extends WordSpec with Matchers with ScalatestRouteTest
  with FailFastCirceSupport with RequestCodecs with ResponseCodecs with BeforeAndAfterEach {

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

  val apis: Apis = (for {
    repositories <- Repositories.reader.local[TestConfig](_ => ())
    executors <- Executors.reader.local[TestConfig](_.executors)
    services = Services.reader.local[TestConfig] {
      c => (c.services, executors, repositories)
    }
    apis <- services andThen Apis.reader
  } yield apis).run(config)


  val usersRestApi: HttpApi = apis.userRestApi

  override protected def beforeEach(): Unit = {
    apis.services.repositories.userRepository.drop()
  }

  private def withUser[T](name: String)(body: User => T): T = {
    val request = SignUpRequest(
      UserName(name),
      EmailAddress(s"$name@fake.com"),
      Some(Password("1234567"))
    )

    Put("/user", marshal(request)) ~> usersRestApi.routes ~> check {
      val user = responseAs[User]
      val result = body(user)
      Delete(s"/user/${user.id}") ~> usersRestApi.routes ~> runRoute
      result
    }
  }

  "Rest api" should {

    "create user record for PUT request" in withUser("john") { user =>

      user shouldBe User(
        user.id,
        UserName("john"),
        EmailAddress("john@fake.com"),
        None,
        user.metadata
      )
    }

    "get user by id for GET request" in withUser("Jim") { user =>

      Get(s"/user/${user.id}") ~> usersRestApi.routes ~> check {
        responseAs[User] shouldBe user
      }
    }

    "update user's email by id for POST request" in withUser("jack") { user =>

      val newEmail = EmailAddress("new@email.com")
      val request = UpdateEmailRequest(newEmail)

      Post(s"/user/${user.id}/email", marshal(request)) ~> usersRestApi.routes ~> check {
        val newUser = responseAs[User]
        newUser.emailAddress shouldBe newEmail
        newUser.metadata.version shouldBe user.metadata.version + 1
      }
    }

    "update user's password by id for POST request" in withUser("jane") { user =>

      val newEmail = Password("654321")
      val request = UpdatePasswordRequest(newEmail)

      Post(s"/user/${user.id}/password", marshal(request)) ~> usersRestApi.routes ~> check {
        responseAs[User].metadata.version shouldBe user.metadata.version + 1
      }
    }

    "reset user's password by id for DELETE request" in withUser("johny") { user =>

      Delete(s"/user/${user.id}/password") ~> usersRestApi.routes ~> check {
        val newUser = responseAs[User]
        newUser.metadata.version shouldBe user.metadata.version + 1
      }
    }

    "block user by id for POST request" in withUser("jake") { user =>

      Post(s"/user/${user.id}/block") ~> usersRestApi.routes ~> check {
        val newUser = responseAs[User]
        newUser.metadata.blockedAt shouldBe 'defined
        newUser.metadata.version shouldBe user.metadata.version + 1
      }
    }

    "unblock user by id for DELETE request" in withUser("jimbo") { user =>

      Post(s"/user/${user.id}/block") ~> usersRestApi.routes ~> runRoute
      Delete(s"/user/${user.id}/block") ~> usersRestApi.routes ~> check {
        val newUser = responseAs[User]
        newUser.metadata.blockedAt shouldBe 'empty
        newUser.metadata.version shouldBe user.metadata.version + 2
      }
    }

    "delete user by id for DELETE request" in withUser("jerome") { user =>

      Post(s"/user/${user.id}/block") ~> usersRestApi.routes ~> runRoute
      Delete(s"/user/${user.id}") ~> usersRestApi.routes ~> check {
        response.status.intValue() shouldBe 200
      }
      Get(s"/user/${user.id}") ~> usersRestApi.routes ~> check {
        responseAs[User].metadata.deletedAt shouldBe 'defined
      }
    }

    "get all user by id for GET request" in withUser("jeckyl") { user =>

      Get(s"/users") ~> usersRestApi.routes ~> check {
        responseAs[List[User]].filterNot(_.metadata.deletedAt.isDefined).size shouldBe 1
      }
    }
  }
}
