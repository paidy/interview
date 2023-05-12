package forex.programs.rates

import io.circe.{ Encoder, Json }
import io.circe.generic.extras.Configuration
import sttp.model.StatusCode

import java.util.UUID

object errors {

  sealed abstract class ForexError(val apiMsg: String, val apiCode: StatusCode) extends Product {
    def apiErrorId: UUID = UUID.randomUUID()
    def logMsg: String
  }

  object ForexError {

    implicit val configuration: Configuration = Configuration.default.withSnakeCaseMemberNames

    implicit val forexErrorEncoder: Encoder[ForexError] = new Encoder[ForexError] {
      final def apply(err: ForexError): Json = Json.obj(
        ("id", Json.fromString(err.apiErrorId.toString)),
        ("message", Json.fromString(err.apiMsg))
      )
    }

    final case class ExternalServiceError(logMsg: String)
        extends ForexError("Uplink service error", StatusCode.BadGateway)

    final case class InternalError(logMsg: String, override val apiMsg: String)
        extends ForexError(apiMsg, StatusCode.InternalServerError)

    final case class RateLookupFailed(logMsg: String, override val apiMsg: String, override val apiCode: StatusCode)
        extends ForexError(apiMsg, apiCode)

    final case class DatabaseError(logMsg: String) extends ForexError("Database Error", StatusCode.InternalServerError)
  }

}
