package forex.services.rates.interpreters

import cats.effect.{Concurrent, ContextShift, IO}
import cats.implicits.catsSyntaxEitherId
import forex.domain.Currency.show
import forex.domain.Rate
import forex.http.rates.Protocol.oneFrameRateDecoder
import forex.services.rates.BatchedAlgebra
import forex.services.rates.Interpreters.OneFrameResponse
import forex.services.rates.errors.Error
import forex.services.rates.errors.Error.OneFrameLookupFailed
import org.http4s.Header.Raw
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Headers, Request}

import scala.concurrent.ExecutionContext.global

class OneFrameBatched[F[_]: Concurrent] extends BatchedAlgebra[F] {
  override def get(pairs: Seq[Rate.Pair]): F[Error Either Seq[Rate]] = {
    apiCallEffect(pairs).to[F]
  }

  def apiCallEffect(pairs: Seq[Rate.Pair]): IO[Error Either Seq[Rate]] = BlazeClientBuilder[IO](global).resource.use { client =>

    val request = Request[IO](
      uri = uri"http://localhost:8080/rates".withQueryParam("pair", values = currencyPairStrings(pairs)),
      // TODO: move token to config
      headers = Headers.of(Raw(name = CaseInsensitiveString("token"), value = "10dc303535874aeccc86a8251e6992f5"))
    )

    client.expect[Seq[OneFrameResponse]](request).redeem(
      ex => {
        OneFrameLookupFailed(ex.getMessage).asLeft[Seq[Rate]]
      },
      value => {
        value.map(_.asRate).asRight[Error]
      }
    )
  }

  def currencyPairStrings(pairs: Seq[Rate.Pair]) = pairs.map(pair => s"${show.show(pair.from)}${show.show(pair.to)}")

  implicit val cs: ContextShift[IO] = IO.contextShift(global)
}
