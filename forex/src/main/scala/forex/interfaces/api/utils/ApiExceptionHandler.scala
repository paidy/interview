package forex.interfaces.api.utils

import akka.http.scaladsl._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import forex.processes._

object ApiExceptionHandler {

  def apply(): server.ExceptionHandler =
    server.ExceptionHandler {
      case RatesError.CurrentRateNotAvailable =>
        ctx =>
          ctx.complete(HttpResponse(
            status = StatusCodes.InternalServerError,
            entity = "Current rate for provided pair in not available"
          ))
      case _: RatesError ⇒
        ctx ⇒
          ctx.complete("Something went wrong in the rates process")
      case _: Throwable ⇒
        ctx ⇒
          ctx.complete("Something else went wrong")
    }

}
