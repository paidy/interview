package forex.main

import forex.config._
import monix.eval.Task
import org.atnos.eff.syntax.addon.monix.task._
import org.zalando.grafter.macros._

@readerOf[ApplicationConfig]
case class Runners() {

  def runApp[R](
      app: AppEffect[R]
  ): Task[R] =
    app.runAsync

}
