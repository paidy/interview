package forex.clients.rates

import cats.Functor
import cats.effect.IO
import cats.implicits._
import cats.effect.kernel.Async
import forex.model.config.OneFrameClientConfig
import forex.model.http.Protocol.OneFrameRate
import forex.model.domain.Rate
import org.http4s.{Header, Headers, Method, QueryParam, QueryParamEncoder, QueryParamKeyLike, Request, Uri}
import org.http4s.blaze.client.BlazeClientBuilder
import forex.model.http.Marshalling._
import forex.model.http.Protocol._
import io.circe.parser.decode
import org.http4s.headers.Authorization
import org.http4s.implicits.http4sLiteralsSyntax
import org.typelevel.ci.CIString
import org.http4s.QueryParamEncoder._
import org.http4s.QueryParamKeyLike.stringKey



class OneFrameClient[F[_] : Async](config: OneFrameClientConfig) extends Algebra[F] {

  private val baseUri = Uri.fromString(s"${config.host}:${config.port}/rates")
    .fold(
      err => throw new RuntimeException(s"Can't build OneFrame URI from config $config", err),
      res => res
    )

  override def get(ratePairs: Set[Rate.Pair], token: String): F[List[OneFrameRate]] = BlazeClientBuilder[F]
    .withConnectTimeout(config.timeout).resource
    .use { client =>
      val params = Map("pair" -> ratePairs.map(_.joined).toSeq)

      val request = Request[F](
        method = Method.GET,
        uri = baseUri.withMultiValueQueryParams(params),
        headers = Headers(Header.Raw(CIString("token"), token))
      )

    client.expect[List[OneFrameRate]](request)
  }
}


object OneFrameClient {

  def apply[F[_] : Async](
                             config: OneFrameClientConfig
                           ): Algebra[F] = new OneFrameClient[F](config)
}
