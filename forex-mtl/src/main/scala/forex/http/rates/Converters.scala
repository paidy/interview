package forex.http.rates

import forex.domain._
import forex.programs.rates.errors.{ Error => ProgramError }

object Converters {
  import Protocol._

  private[rates] implicit class GetApiResponseOps(val rate: Rate) extends AnyVal {
    def asGetApiResponse: GetApiResponse =
      GetApiResponse(
        from = rate.pair.from,
        to = rate.pair.to,
        price = rate.price,
        timestamp = rate.timestamp
      )
  }

  private[rates] implicit class ApiErrorOps(val error: ProgramError) extends AnyVal {
    def asApiError: ApiError =
      ApiError(
        errorType = error.getClass.getSimpleName,
        errorMsg = error.getMessage
      )
  }

}
