package forex.services.rates.interpreters

import java.time.OffsetDateTime

import cats.effect.{ContextShift, IO}
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.errors.Error
import forex.services.rates.Algebra
import org.http4s.{EntityDecoder, Header, Headers, Request, Uri}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.io._
import org.http4s.circe._
import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.services.rates.errors.Error.OneFrameLookupFailed
import io.circe.{Decoder, HCursor}

import scala.concurrent.ExecutionContext
import scala.util.Try

class OneFrameSimple[F[_] : Applicative](baseUri: Uri, token: String) extends Algebra[F] {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  implicit val currencyDecoder: Decoder[Currency] = Decoder.decodeString.emapTry(str => Try(Currency.fromString(str)))
  implicit val timeStampDecoder: Decoder[OffsetDateTime] = Decoder.decodeString.emapTry(str => Try(OffsetDateTime.parse(str)))

  implicit val rateDecoder: Decoder[Rate] = (c: HCursor) => for {
    from <- c.downField("from").as[Currency]
    to <- c.downField("to").as[Currency]
    price <- c.downField("price").as[BigDecimal]
    timestamp <- c.downField("time_stamp").as[OffsetDateTime]
  } yield {
    Rate(Rate.Pair(from, to), Price(price), Timestamp(timestamp))
  }

  implicit val rateEntityDecoder: EntityDecoder[IO, List[Rate]] = jsonOf[IO, List[Rate]]

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    val uriWithParam = baseUri.withQueryParam("pair", s"${pair.from}${pair.to}")
    val req = Request[IO](method = GET, uri = uriWithParam , headers = Headers.of(Header("token", token)))
    val io = BlazeClientBuilder[IO](ExecutionContext.global).resource.use(c => c.expect[List[Rate]](req))
    val result = io.attempt.unsafeRunSync()
    val result2 = result match {
      case Left(t) => OneFrameLookupFailed(t.getMessage).asLeft[Rate]
      case Right(r) => r.head.asRight[Error]
    }
    result2.pure
  }
}
