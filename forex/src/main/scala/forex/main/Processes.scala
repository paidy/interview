package forex.main

import cats.Eval
import com.typesafe.scalalogging.LazyLogging
import forex.config._
import forex.domain.Rate
import forex.{services => s}
import forex.{processes => p}
import org.zalando.grafter.{Start, StartResult, Stop, StopResult}
import org.zalando.grafter.macros._

import scala.concurrent.duration._
import org.atnos.eff.syntax.addon.monix.task._
@readerOf[ApplicationConfig]
case class Processes(
                    forexConfig: ForexConfig,
                    executors: Executors
                    ) extends Start with Stop with LazyLogging {

  implicit final lazy val _oneForge: s.OneForge[AppEffect] =
    s.OneForge.dummy[AppStack]

  implicit val _ = executors.default

  final val Rates = p.Rates[AppEffect]

  final lazy val updatesSwitch =
    executors
      .default
      .scheduleWithFixedDelay(
        0.seconds,
        forexConfig.delay
      )(
        Rates.updateRates(Rate.Pair.allSupportedPairs)
          .map{
            case Left(error)=> logger.warn(s"Unable to refresh rates", error)
            case Right(_) => logger.info("Refreshed rates")
          }
          .runAsync
          .runAsync
      )

  override def start: Eval[StartResult] =
    StartResult.eval("Processes"){
      updatesSwitch
    }

  override def stop: Eval[StopResult] =
    StopResult.eval("Processors"){
      updatesSwitch.cancel()
    }
}
