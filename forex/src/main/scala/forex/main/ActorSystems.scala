package forex.main

import akka.actor._
import akka.stream._
import cats.Eval
import forex.config._
import org.zalando.grafter._
import org.zalando.grafter.macros._

@readerOf[ApplicationConfig]
case class ActorSystems(
    config: AkkaConfig
) extends Start
    with Stop {

  implicit lazy val system: ActorSystem =
    ActorSystem(config.name)

  implicit lazy val materializer: Materializer =
    ActorMaterializer()(system)

  override def start: Eval[StartResult] =
    StartResult.eval("ActorSystems") {
      if (config.exitJvmTimeout.isDefined) {
        system.registerOnTermination(System.exit(0))
      }
    }

  override def stop: Eval[StopResult] =
    StopResult.eval("ActorSystems") {}

}
