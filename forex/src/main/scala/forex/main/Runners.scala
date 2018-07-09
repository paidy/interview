package forex.main

import forex.config._
import monix.eval.Task
import org.atnos.eff.syntax.addon.monix.task._
import org.zalando.grafter.macros._

@readerOf[ApplicationConfig]
case class Runners(
                    caches: Caches
                  ) {

  def runApp[R](
      app: AppEffect[R]
  ): Task[R] =
    app.runTaskMemo(caches.hashMapRatesCache).runAsync

}
