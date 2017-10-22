package users.api

import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.{BaseCirceSupport, FailFastUnmarshaller}
import users.services._

import scala.concurrent.Future

case class UsersRestApi(
  userManagement: UserManagement[Future[?]]
) extends HttpApi with BaseCirceSupport with FailFastUnmarshaller with RequestCodecs with ResponseCodecs {

  override def routes: Route = {
    ???
  }
}
