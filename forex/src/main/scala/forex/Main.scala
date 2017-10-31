package forex

import cats.Eval
import com.typesafe.scalalogging._
import forex.config._
import forex.main._
import org.zalando.grafter._

object Main extends App with LazyLogging {

  var app: Option[Application] = None

  pureconfig.loadConfig[ApplicationConfig]("app") match {
    case Left(errors) ⇒
      logger.error(s"Errors loading the configuration:\n${errors.toList.mkString("- ", "\n- ", "")}")
    case Right(applicationConfig) ⇒
      val application = configure[Application](applicationConfig).configure()

      Rewriter
        .startAll(application)
        .flatMap {
          case results if results.exists(!_.success) ⇒
            logger.error(toStartErrorString(results))
            Rewriter.stopAll(application).map(_ ⇒ ())
          case results ⇒
            logger.info(toStartSuccessString(results))
            Eval.now {
              app = Some(application)
            }
        }
        .value
  }

  sys.addShutdownHook {
    app.foreach(Rewriter.stopAll(_))
  }

}
