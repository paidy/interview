package users.main

import cats.data.Reader
import users.api.{HttpApi, UsersRestApi}
import users.config.ApplicationConfig

object Apis {
  val reader: Reader[Services, Apis] =
    Reader(Apis(_))

  val fromApplicationConfig: Reader[ApplicationConfig, Apis] = {
    Services.fromApplicationConfig andThen reader
  }
}

case class Apis(
  services: Services
) {

  val userRestApi: HttpApi = UsersRestApi(services.userManagement)

}
