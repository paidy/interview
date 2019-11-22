package forex.main

import forex.config._
import forex.{ services ⇒ s }
import forex.{ processes ⇒ p }
import org.zalando.grafter.macros._

@readerOf[ApplicationConfig]
case class Processes() {

  implicit final lazy val _oneFrame: s.OneFrame[AppEffect] =
    s.OneFrame.dummy[AppStack]

  final val Rates = p.Rates[AppEffect]

}
