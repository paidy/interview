package users.http.validation

import users.services.usermanagement.Error

sealed trait ValidationError:
  def error: String

object ValidationError:

  def fromPersistenceError(err: Error): ValidationError = err match
    case Error.Active => UnprocessableRequest("The user is active already")
    case Error.Blocked => UnprocessableRequest("The user is blocked already")
    case Error.Deleted => UnprocessableRequest("The user is deleted already")
    case Error.Exists => UnprocessableRequest("Such username is already exists")
    case Error.NotFound => UserNotFound
    case Error.System(error) => InternalError(error)

case object InvalidEmail extends ValidationError:
  val error = "Email is incorrect"

case object InvalidRequest extends ValidationError:
  val error = "Can't decode json"

case class UnprocessableRequest(error: String) extends ValidationError

case object UserNotFound extends ValidationError:
  val error = "The user was not found"

case class InternalError(underlying: Throwable) extends ValidationError:
  val error = "We've got an incident."
