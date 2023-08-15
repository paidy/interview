package users.http

import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

import cats.*
import cats.data.*
import cats.effect.Async
import cats.implicits.*

import users.domain.*
import users.http.dto.*
import users.http.validation.*
import users.services.usermanagement.Error
import users.services.UserManagement

object UserRoutes:
  def make[F[_]: Async: Logger](service: UserManagement[F]): Routes[F] = new UserRoutes[F](service)

final class UserRoutes[F[_]: Async: Logger](val userService: UserManagement[F])
    extends Routes[F]
    with Http4sDsl[F]
    with Auth[F]
    with RouteHelpers[F]:

  import users.domain.Protocol.*

  private val publicRoutes = HttpRoutes.of[F] { case req @ POST -> Root / "signup" =>
    val f: EitherT[F, ValidationError, User] = for
      data <- EitherT(req.as[SignupForm].attempt).leftMap(_ => InvalidRequest: ValidationError)
      validated <- EitherT.fromEither(Validation.validateSignupForm(data))
      user <- EitherT(userService.signUp(data.userName, data.emailAddress, data.password))
                .leftMap(ValidationError.fromPersistenceError)
    yield user

    f.foldF(
      errorToResponse,
      u =>
        Response(
          headers = Headers(tokenHeader(u.id))
        ).withEntity(u.short).pure[F]
    )
  }

  private val authedRoutes: AuthedRoutes[User, F] = AuthedRoutes.of {
    case GET -> Root / "me" as user => Ok(user)
    case req @ POST -> Root / "update-email" as user =>
      val f = for
        data <- EitherT(req.req.as[UpdateEmail].attempt).leftMap(_ => InvalidRequest: ValidationError)
        validated <- EitherT.fromEither(Validation.validateEmail(data.emailAddress))
        user <- EitherT(userService.updateEmail(user.id, validated)).leftMap(ValidationError.fromPersistenceError)
      yield user
      f.foldF(
        errorToResponse,
        u =>
          Response(
            headers = Headers(tokenHeader(u.id))
          ).withEntity(u.short).pure[F]
      )
    case req @ POST -> Root / "update-password" as user =>
      val f = for
        data <- EitherT(req.req.as[UpdatePassword].attempt).leftMap(_ => InvalidRequest: ValidationError)
        user <-
          EitherT(userService.updatePassword(user.id, data.password)).leftMap(ValidationError.fromPersistenceError)
      yield user
      f.foldF(
        errorToResponse,
        u => Response(headers = Headers(tokenHeader(u.id))).withEntity(u.short).pure[F]
      )

    case POST -> Root / "reset-password" as user =>
      val f =
        for user <- EitherT(userService.resetPassword(user.id)).leftMap(ValidationError.fromPersistenceError)
        yield user
      f.foldF(
        errorToResponse,
        u => Response(headers = Headers(tokenHeader(u.id))).withEntity(u.short).pure[F]
      )
  }

  val routes = publicRoutes <+> authMiddleware(authedRoutes)
