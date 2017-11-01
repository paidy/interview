package forex.processes.rates

import forex.services._

package object converters {
  import messages._

  def toProcessError[T <: Throwable](t: T): Error = t match {
    case OneForgeError.Generic     ⇒ Error.Generic
    case OneForgeError.System(err) ⇒ Error.System(err)
    case e: Error                  ⇒ e
    case e                         ⇒ Error.System(e)
  }

}
