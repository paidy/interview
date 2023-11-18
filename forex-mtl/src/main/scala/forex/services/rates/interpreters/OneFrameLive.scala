package forex.services.rates.interpreters

import cats.Applicative
import cats.data.EitherT
import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.services.rates.{Algebra, errors}
import scalaj.http.Http
import io.circe.parser.decode
import io.circe.generic.auto._

import java.time.{Instant, OffsetDateTime, ZoneId}

class OneFrameLive[F[_] : Applicative] extends Algebra[F] {
  private case class OneFrameResponse(from: String, to: String, bid: Double, ask: Double, price: Double, time_stamp: String) {
    def toRate: Rate = {
      val parsedTime = OffsetDateTime.ofInstant(Instant.parse(time_stamp), ZoneId.systemDefault())
      Rate(Rate.Pair(Currency.fromString(from), Currency.fromString(to)), Price(price), Timestamp(parsedTime))
    }
  }

  override def get(pair: Rate.Pair): F[Either[errors.Error, Rate]] = {
    val response = executeGetRequest("http://localhost:8080/rates", Seq(("pair", pair.combine)))
    EitherT.fromEither[F](response).value
  }

  def executeGetRequest(uri: String, parameters: Seq[(String, String)]) = {
    val response = Http(uri).header("token", "10dc303535874aeccc86a8251e6992f5").params(parameters).asString
    println(response.body)
    decode[List[OneFrameResponse]](response.body) match {
      case Left(res) =>
        Left(errors.Error.OneFrameLookupFailed(res.getMessage): errors.Error)
      case Right(res) =>  Right(res.head.toRate)
    }
  }
}
