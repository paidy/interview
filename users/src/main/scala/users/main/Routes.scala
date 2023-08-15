package users.main

import org.http4s.server.middleware.*
import org.typelevel.log4cats.Logger

import cats.effect.*
import cats.implicits.*

import scala.concurrent.duration.*

import users.http.{AdminRoutes, UserRoutes}
import users.services.UserManagement

object Routes:

  def make[F[_]: Async: Logger](service: UserManagement[F]) =
    ErrorHandling.Recover.total(
      Timeout(5.seconds)(
        List(
          UserRoutes.make[F],
          AdminRoutes.make[F]
        ).map(_.apply(service)).map(_.routes).reduce(_ <+> _).orNotFound
      )
    )
