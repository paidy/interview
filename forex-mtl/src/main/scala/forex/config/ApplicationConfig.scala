package forex.config

import org.http4s.Uri
import org.http4s.Uri.{Authority, IPv4}

import scala.concurrent.duration.FiniteDuration

case class ApplicationConfig(
    http: HttpConfig,
    source: RateSource
)

case class HttpConfig(
    host: String,
    port: Int,
    timeout: FiniteDuration
)

sealed trait RateSource
case object Dummy extends RateSource
case class Simple(uriInfo: UriInfo, token: String) extends RateSource
case class Cached(uriInfo: UriInfo, token: String, refreshTime: Int) extends RateSource

sealed trait UriInfo{
  def getUri: Uri
}
case class IpFourUri(hostIp: String, port: Int, path: String) extends UriInfo {
  def getUri: Uri = Uri(authority = Some(Authority(host = IPv4(hostIp), port = Some(port))), path = path)
}