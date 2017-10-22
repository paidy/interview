package users.api

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, _}
import io.circe.Encoder
import users.domain._
import users.services._

import scala.concurrent.Future

case class UsersRestApi(
  default: UserManagement[Future[?]],
  restricted: UserManagement[Future[?]]
) extends HttpApi with ErrorHandling with DefaultCirceSupport with RequestCodecs with ResponseCodecs {

  private val userId = Segment.map(UserId)

  override def routes: Route = handleExceptions(errorHandler) {
    userManagementRoutes(restricted)(RestrictedResponseCodecs.userEncoder) ~ pathPrefix("admin") {
      userManagementRoutes(default)(DefaultResponseCodecs.userEncoder)
    }
  }

  private def userManagementRoutes(
    userManagement: UserManagement[Future[?]]
  )(implicit dec: Encoder[User]): Route = {
    pathPrefix("user") {

      (put & entity(as[SignUpRequest])) { request =>

        complete {
          userManagement.signUp(
            request.userName,
            request.emailAddress,
            request.password
          )
        }
      } ~ (delete & path(userId) & pathEnd) { id =>

        complete {
          userManagement.delete(id)
        }
      } ~ (get & path(userId)) { id =>

        complete {
          userManagement.get(id)
        }
      } ~ (post & path(userId / "email") & entity(as[UpdateEmailRequest])) { (id, request) =>

        complete {
          userManagement.updateEmail(
            id, request.email
          )
        }
      } ~ (post & path(userId / "password") & entity(as[UpdatePasswordRequest])) { (id, request) =>

        complete {
          userManagement.updatePassword(
            id, request.password
          )
        }
      } ~ (delete & path(userId / "password")) { id =>

        complete {
          userManagement.resetPassword(id)
        }
      } ~ (post & path(userId / "block")) { id =>

        complete {
          userManagement.block(id)
        }
      } ~ (delete & path(userId / "block")) { id =>

        complete {
          userManagement.unblock(id)
        }
      }
    } ~ pathPrefix("users") {
      (get & pathEnd) {
        complete {
          userManagement.all()
        }
      }
    }
  }
}
