package users.main

import cats.data._

import users.config._
import users.services._

import scala.concurrent.Future

object Services {
  val reader: Reader[(ServicesConfig, Executors, Repositories), Services] =
    Reader((Services.apply _).tupled)

  val fromApplicationConfig: Reader[ApplicationConfig, Services] =
    (for {
      config ← ServicesConfig.fromApplicationConfig
      executors ← Executors.fromApplicationConfig
      repositories ← Repositories.fromApplicationConfig
    } yield (config, executors, repositories)) andThen reader
}

final case class Services(
    config: ServicesConfig,
    executors: Executors,
    repositories: Repositories
) {
  import executors._
  import repositories._

  implicit val ec = serviceExecutor

  final val userManagement: UserManagement[Future[?]] =
    UserManagement.unreliable(
      UserManagement.default(userRepository),
      config.users
    )

}
