package forex.client

import cats.implicits.toShow
import forex.domain.Rate.Pair
import sttp.model.Uri
import sttp.client4.Response
import sttp.client4.quick.quickRequest
import sttp.client4.quick._
import scala.util.{Try, Success, Failure}

class OneFrameHttpClient extends HttpClient {
  def getApiResponse(uri: Uri): Either[String, Response[String]] = {
    Try {
      quickRequest
        .get(uri)
        .header("Content-Type", "application/json")
        .header("token", "10dc303535874aeccc86a8251e6992f5")
        .send()
    } match {
      case Success(response) =>
        Right(response)
      case Failure(exception) =>
        Left(s"Error calling backend service: ${exception.getMessage}")
    }
  }

  def getRates(pair: Pair): Either[String, Response[String]] = {
    getApiResponse(getRatesUri(pair))
  }

  def getRatesUri(pair: Pair): Uri = {
    val param = pair.to.show + pair.from.show
    uri"http://localhost:8081/rates?pair=$param"
  }
}

object OneFrameHttpClient {
  private def apply: OneFrameHttpClient = new OneFrameHttpClient()

  private val instance: OneFrameHttpClient = apply

  def getInstance: OneFrameHttpClient = instance
}
