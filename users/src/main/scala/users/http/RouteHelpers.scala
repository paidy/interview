package users.http

import org.http4s.dsl.Http4sDsl
import org.http4s.Header
import org.http4s.Response
import org.typelevel.ci.CIString

import cats.Applicative

import users.domain.User
import users.http.validation.*
import users.services.usermanagement

trait RouteHelpers[F[_]: Applicative]:
  self: Http4sDsl[F] =>

  protected val tokenHeader: User.Id => Header.Raw = id => Header.Raw(CIString("token"), id.value)

  protected def errorToResponse(error: ValidationError | usermanagement.Error): F[Response[F]] = error match
    case InvalidEmail => BadRequest(InvalidEmail.error)
    case InvalidRequest => BadRequest(InvalidRequest.error)
    case UnprocessableRequest(error) => BadRequest(error)
    case UserNotFound => BadRequest(UserNotFound.error)
    case err: InternalError => UnprocessableEntity(err.error)
    case usermanagement.Error.System(underlying) => UnprocessableEntity("Internal error happened")
    case usermanagement.Error.Active => BadRequest("The user is active already")
    case usermanagement.Error.Blocked => BadRequest("The user is blocked already")
    case usermanagement.Error.Deleted => NotFound("The user does not exist")
    case usermanagement.Error.Exists => BadRequest("A user with this username is already exist")
    case usermanagement.Error.NotFound => NotFound("The user does not exist")
