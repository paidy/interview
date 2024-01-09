package users

import java.time.OffsetDateTime

import org.http4s.server.Server
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.syntax._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.SelfAwareStructuredLogger

import cats.*
import cats.effect.*
import cats.implicits.*

import fs2.io.net.Network
import users.config.*
import users.domain.*
import users.main.*

object Main extends IOApp:

  private val adminUsername = UserName("admin")

  val config = ApplicationConfig(
    executors = ExecutorsConfig(
      services = ExecutorsConfig.ServicesConfig(
        parallellism = 4
      )
    ),
    services = ServicesConfig(
      users = ServicesConfig.UsersConfig(
        failureProbability = 0.1,
        timeoutProbability = 0.1
      )
    )
  )

  override def run(args: List[String]): IO[ExitCode] = {
    implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]
    implicit val logger: SelfAwareStructuredLogger[IO] = loggerFactory.getLogger
    (for
      app <- appBuilder[IO](config)
      server <- program[IO](app)
      admin <- Resource.eval(generateAdmin[IO](app))
    yield server)
      .use(_ => IO.never)
      .as(ExitCode.Success)

  }

  private def appBuilder[F[_]: Async](config: ApplicationConfig): Resource[F, Application[F]] =
    Resource.eval(Application.fromApplicationConfig[F].run(config))

  private def program[F[_]: Async: Network: LoggerFactory: Logger](app: Application[F]): Resource[F, Server] =
    app.services.httpService.server[F](app.services.userManagement)

  private def generateAdmin[F[_]: Monad: Logger](app: Application[F]): F[User] =
    app.services.repositories.userRepository.getByUserName(adminUsername).flatMap {
      case Some(u) => u.pure[F]
      case None =>
        for {
          _ <- info"Creating admin"
          id <- app.services.userManagement.generateId()
          now = OffsetDateTime.now
          u = User(
                id,
                UserName("admin"),
                EmailAddress("admin@users.com"),
                Some(Password("admin")),
                User.Metadata(1, now, now, None, None),
                isAdmin = true
              )
          _ <- app.services.repositories.userRepository.insert(u)
          _ <- info"Admin auth info header: Token: ${id.value}"
        } yield u
    }
