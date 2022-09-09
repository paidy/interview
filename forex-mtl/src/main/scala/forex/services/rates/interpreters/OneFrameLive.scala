package forex.services.rates.interpreters

import cats.effect.{Concurrent, ContextShift, IO}
import cats.syntax.either._
import forex.domain.Currency.show
import forex.domain.Rate
import forex.http.rates.Protocol.oneFrameRateDecoder
import forex.services.rates.Algebra
import forex.services.rates.Interpreters.OneFrameResponse
import forex.services.rates.errors.Error
import forex.services.rates.errors.Error.OneFrameLookupFailed
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.Header.Raw
import org.http4s.circe.jsonOf
import org.http4s.client.blaze._
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.util.CaseInsensitiveString
import org.http4s.{EntityDecoder, Headers, Request}

import scala.concurrent.ExecutionContext.global

class OneFrameLive[F[_]: Concurrent] extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    apiCallEffect(pair).to[F]
  }

  def apiCallEffect(pair: Rate.Pair): IO[Error Either Rate] = BlazeClientBuilder[IO](global).resource.use { client =>
    val currencyPair = s"${show.show(pair.from)}${show.show(pair.to)}"
    val request = Request[IO](
      uri = uri"http://localhost:8080/rates".withQueryParam("pair", currencyPair),
//      uri = Uri(
//        scheme = Scheme.http.pure[Option],
//        authority = Uri.Authority(host = Uri.RegName("localhost:8000")).pure[Option],
//        path = "http://localhost:8080/rates",
//        query = Query.fromPairs(("pair", s"${show.show(pair.from)}${show.show(pair.to)}"))
//      ),
      headers = Headers.of(Raw(name = CaseInsensitiveString("token"), value = "10dc303535874aeccc86a8251e6992f5"))
    )
//    println(request.toString())
    client.expect[Seq[OneFrameResponse]](request).redeem(
      ex => {
        println(ex)
        OneFrameLookupFailed(ex.getMessage).asLeft[Rate]
      },
      value => {
        println(value)
        value.head.asRate.asRight[Error]
      }
    )
  }

  implicit val cs: ContextShift[IO] = IO.contextShift(global)

}

