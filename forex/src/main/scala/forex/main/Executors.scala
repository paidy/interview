package forex.main

import cats.Eval
import forex.config._
import monix.execution.Scheduler
import org.zalando.grafter._
import org.zalando.grafter.macros._

@readerOf[ApplicationConfig]
case class Executors(
    config: ExecutorsConfig,
    actorSystems: ActorSystems
) extends Start {
  import actorSystems._

  implicit lazy val default: Scheduler =
    Scheduler(system.dispatchers.lookup(config.default))

  override def start: Eval[StartResult] =
    StartResult.eval("Executors") {
      default
    }

}
