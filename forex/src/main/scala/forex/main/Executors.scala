package forex.main

import cats.Eval
import forex.config._
import org.zalando.grafter._
import org.zalando.grafter.macros._

import scala.concurrent.ExecutionContext

@readerOf[ApplicationConfig]
case class Executors(
    config: ExecutorsConfig,
    actorSystems: ActorSystems
) extends Start {
  import actorSystems._

  lazy val default: ExecutionContext =
    system.dispatchers.lookup(config.default)

  override def start: Eval[StartResult] =
    StartResult.eval("Executors") {
      default
    }

}
