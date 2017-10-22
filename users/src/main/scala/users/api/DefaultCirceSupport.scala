package users.api

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Printer

trait DefaultCirceSupport extends FailFastCirceSupport {

  implicit val printer: Printer = Printer(
    preserveOrder = true,
    dropNullValues = true,
    indent = "",

  )

}
