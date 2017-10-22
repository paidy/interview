package users.api

import akka.http.scaladsl.server.Route

trait HttpApi {

  def routes: Route

}
