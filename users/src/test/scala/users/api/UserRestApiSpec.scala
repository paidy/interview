package users.api

import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.scalatest.{BeforeAndAfterEach, Suite}
import users.WithApplication
import users.domain.{EmailAddress, Password, User, UserName}

trait UserRestApiSpec extends ScalatestRouteTest
  with FailFastCirceSupport with RequestCodecs
  with DefaultResponseCodecs with BeforeAndAfterEach with WithApplication
{
  requires: Suite =>

  case class UserResponse(
    id: User.Id,
    userName: UserName,
    emailAddress: EmailAddress
  )

  implicit val (userResponseEncoder, userResponseDecoder) = deriveCodecs[UserResponse]

  val usersRestApi: HttpApi = application.apis.userRestApi

  override protected def afterEach(): Unit = {
    application.apis.services.repositories.userRepository.drop()
  }

  protected def withUser[T](name: String)(body: User => T): T = {
    val request = SignUpRequest(
      UserName(name),
      EmailAddress(s"$name@fake.com"),
      Some(Password("1234567"))
    )

    Put("/admin/user", marshal(request)) ~> usersRestApi.routes ~> check(body(responseAs[User]))
  }

}
