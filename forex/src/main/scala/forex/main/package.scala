package forex

import monix.eval.Task
import monix.execution.Scheduler
import org.atnos.eff._
import org.zalando.grafter._

import scala.concurrent.ExecutionContext

package object main {

  type AppStack = Fx.fx1[Task]
  type AppEffect[R] = Eff[AppStack, R]

  def toStartErrorString(results: List[StartResult]): String =
    s"Application startup failed. Modules: ${results
      .collect {
        case StartError(message, ex) ⇒ s"$message [${ex.getMessage}]"
        case StartFailure(message)   ⇒ message
      }
      .mkString(", ")}"

  def toStartSuccessString(results: List[StartResult]): String =
    s"Application startup successful. Modules: ${results
      .collect {
        case StartOk(message) ⇒ message
      }
      .mkString(", ")}"

}
