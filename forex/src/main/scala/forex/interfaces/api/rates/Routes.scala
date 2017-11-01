package forex.interfaces.api.rates

import akka.http.scaladsl._
import forex.config._
import forex.main._
import forex.interfaces.api.utils._
import org.zalando.grafter.macros._

@readerOf[ApplicationConfig]
case class Routes(
    processes: Processes,
    runners: Runners
) {
  import server.Directives._
  import Directives._
  import Converters._
  import ApiMarshallers._

  import processes._
  import runners._

  lazy val route: server.Route =
    get {
      getApiRequest { req ⇒
        complete {
          runApp(
            Rates
              .get(toGetRequest(req))
              .map(_.map(result ⇒ toGetApiResponse(result)))
          )
        }
      }
    }

}
