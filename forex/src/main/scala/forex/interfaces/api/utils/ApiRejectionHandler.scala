package forex.interfaces.api.utils

import akka.http.scaladsl._

object ApiRejectionHandler {

  def apply(): server.RejectionHandler =
    server.RejectionHandler.default

}
