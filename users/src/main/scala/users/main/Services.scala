package users.main

import cats.data.*

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import users.config.*
import users.services.*

object Services {

  val reader: Reader[(ServicesConfig, Executors, Repositories), Services] =
    Reader(Services.apply.tupled)

  val fromApplicationConfig: Reader[ApplicationConfig, Services] =
    (for {
      config <- ServicesConfig.fromApplicationConfig
      executors <- Executors.fromApplicationConfig
      repositories <- Repositories.fromApplicationConfig
    } yield (config, executors, repositories)).andThen(reader)
}

final case class Services(
  config: ServicesConfig,
  executors: Executors,
  repositories: Repositories
) {

  import executors.*
  import repositories.*

  implicit val ec: ExecutionContext = serviceExecutor

  final val userManagement: UserManagement[Future[*]] =
    UserManagement.unreliable(
      UserManagement.default(userRepository),
      config.users
    )

}
