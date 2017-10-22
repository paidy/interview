package users.api

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, _}
import users.services.usermanagement.Error._


trait ErrorHandling {

  val errorHandler = ExceptionHandler {
    case NotFound => completeWithError(StatusCodes.NotFound, "User not found.")
    case Exists => completeWithError(StatusCodes.BadRequest, "User already exists.")
    case Active => completeWithError(StatusCodes.BadRequest, "User is active.")
    case Deleted => completeWithError(StatusCodes.BadRequest, "User is deleted.")
    case Blocked => completeWithError(StatusCodes.BadRequest, "User is blocked.")
    case sys: System => completeWithError(StatusCodes.InternalServerError, sys.underlying.getMessage)
  }

  private def completeWithError(statusCode: StatusCode, message: String) =
    complete(HttpResponse(statusCode, entity = message))

}
