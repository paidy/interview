package users.main

import cats.data.*
import cats.effect.*
import cats.implicits.*

import scala.concurrent.ExecutionContext

import users.config.*
import users.services.*

object Services:

  def reader[F[_]: Async]: ReaderT[F, (ServicesConfig, Executors, Repositories[F], HttpService), Services[F]] =
    ReaderT(Services[F].apply.tupled(_).pure)

  def fromApplicationConfig[F[_]: Async]: ReaderT[F, ApplicationConfig, Services[F]] =
    (for
      config <- ServicesConfig.fromApplicationConfig
      executors <- Executors.fromApplicationConfig
      repositories <- Repositories.fromApplicationConfig
      httpService <- HttpService.fromApplicationConfig
    yield (config, executors, repositories, httpService)).andThen(reader)

final case class Services[F[_]: Async](
  config: ServicesConfig,
  executors: Executors,
  repositories: Repositories[F],
  httpService: HttpService
):

  import executors.*
  import repositories.*

  implicit val ec: ExecutionContext = serviceExecutor

  final val userManagement: UserManagement[F] =
    UserManagement.unreliable[F](
      UserManagement.default[F](userRepository),
      config.users
    )
