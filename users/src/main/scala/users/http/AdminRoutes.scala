package users.http

import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.*
import org.http4s.server.Router

import cats.*
import cats.data.*
import cats.effect.Async
import cats.implicits.*

import users.domain.User
import users.http.dto.*
import users.http.validation.*
import users.services.usermanagement
import users.services.UserManagement

object AdminRoutes:
  def make[F[_]: Async](service: UserManagement[F]): Routes[F] = AdminRoutes[F](service)

final class AdminRoutes[F[_]: Async](val userService: UserManagement[F])
    extends Routes[F]
    with Http4sDsl[F]
    with Auth[F]
    with RouteHelpers[F]:

  import users.domain.Protocol.*

  private implicit val idQueryParam: QueryParamDecoder[User.Id] =
    QueryParamDecoder[String].map(User.Id.apply)

  private object UserIdParam extends QueryParamDecoderMatcher[User.Id]("id")

  private val pathPrefix = "admin"

  private val adminRoutes = AuthedRoutes.of[User, F] {
    case GET -> Root :? UserIdParam(id) as admin =>
      complete(admin)(userService.get(id))
    case req @ POST -> Root / "update-email" :? UserIdParam(id) as admin =>
      val f = for
        data <- EitherT(req.req.as[UpdateEmail].attempt).leftMap(_ => InvalidRequest: ValidationError)
        validated <- EitherT.fromEither(Validation.validateEmail(data.emailAddress))
        user <- EitherT(userService.updateEmail(id, validated)).leftMap(ValidationError.fromPersistenceError)
      yield user
      f.foldF(
        errorToResponse,
        u =>
          Response(
            headers = Headers(tokenHeader(admin.id))
          ).withEntity(u.withoutPassword).pure[F]
      )
    case POST -> Root / "reset-password" :? UserIdParam(id) as admin =>
      complete(admin)(userService.resetPassword(id))
    case POST -> Root / "block" :? UserIdParam(id) as admin =>
      if (id == admin.id) BadRequest("Admin can't block itself")
      else complete(admin)(userService.block(id))
    case POST -> Root / "unblock" :? UserIdParam(id) as admin =>
      if (id == admin.id) BadRequest("Admin can't unblock itself")
      else complete(admin)(userService.unblock(id))
    case DELETE -> Root / "delete" :? UserIdParam(id) as admin =>
      if (id == admin.id) BadRequest("Admin can't delete itself")
      else EitherT(userService.delete(id)).foldF(errorToResponse, _ => Ok())
    case GET -> Root / "all" as admin =>
      EitherT(userService.all()).foldF(
        errorToResponse,
        users =>
          Response(
            headers = Headers(tokenHeader(admin.id))
          ).withEntity(users.map(_.withoutPassword)).pure[F]
      )
  }

  private def complete(admin: User)(func: => F[Either[usermanagement.Error, User]]): F[Response[F]] =
    EitherT(func).foldF(
      errorToResponse,
      user =>
        Response(
          headers = Headers(tokenHeader(admin.id))
        ).withEntity(user.withoutPassword).pure[F]
    )

  val routes = Router(pathPrefix -> adminMiddleware(adminRoutes))
