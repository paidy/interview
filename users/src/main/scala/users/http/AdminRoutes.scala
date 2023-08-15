package users.http

import org.http4s.*
import org.http4s.dsl.*
import org.http4s.server.Router

import cats.*

import users.domain.User
import users.services.UserManagement

object AdminRoutes:
  def make[F[_]: Monad](service: UserManagement[F]): Routes[F] = AdminRoutes[F](service)

final class AdminRoutes[F[_]: Monad](val userService: UserManagement[F])
    extends Routes[F]
    with Http4sDsl[F]
    with Auth[F]:
  private val pathPrefix = "admin"

  private val adminRoutes = AuthedRoutes.of[User, F] { case GET -> Root as user =>
    Ok(userService.generateId().toString())
  }

  val routes = Router(pathPrefix -> adminMiddleware(adminRoutes))
