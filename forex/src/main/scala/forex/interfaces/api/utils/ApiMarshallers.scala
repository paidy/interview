package forex.interfaces.api.utils

import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import marshalling._

trait ApiMarshallers extends EffTaskSupport

object ApiMarshallers extends EffTaskSupport with ErrorAccumulatingCirceSupport
