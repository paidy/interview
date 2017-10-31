package forex.main

import scala.concurrent.Future
import akka.http.scaladsl._
import cats.Eval
import org.zalando.grafter._
import org.zalando.grafter.macros._
import forex.config._
import forex.interfaces.api.Routes

@readerOf[ApplicationConfig]
case class Api(
    config: ApiConfig,
    actorSystems: ActorSystems,
    executors: Executors,
    routes: Routes
) extends Start
    with Stop {
  import actorSystems._

  implicit private val ec =
    executors.default

  private lazy val bindingFuture: Future[Http.ServerBinding] =
    Http().bindAndHandle(routes.route, config.interface, config.port)

  def start: Eval[StartResult] =
    StartResult.eval("API")(bindingFuture)

  def stop: Eval[StopResult] =
    StopResult.eval("API") {
      for {
        binding ← bindingFuture
      } binding.unbind()
    }
}
