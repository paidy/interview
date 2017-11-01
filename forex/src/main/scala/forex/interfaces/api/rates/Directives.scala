package forex.interfaces.api.rates

import akka.http.scaladsl._
import forex.domain._

trait Directives {
  import server.Directives._
  import unmarshalling.Unmarshaller
  import Protocol._

  def getApiRequest: server.Directive1[GetApiRequest] =
    for {
      from ← parameter('from.as(currency))
      to ← parameter('to.as(currency))
    } yield GetApiRequest(from, to)

  private val currency =
    Unmarshaller.strict[String, Currency](Currency.fromString)

}

object Directives extends Directives
