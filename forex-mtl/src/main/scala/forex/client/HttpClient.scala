package forex.client

import sttp.client4.Response
import sttp.model.Uri

trait HttpClient {
  def getApiResponse(url: Uri): Either[String, Response[String]]
}
