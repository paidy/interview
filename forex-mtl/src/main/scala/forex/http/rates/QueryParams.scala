package forex.http.rates

import cats.implicits._
import forex.domain.Currency
import org.http4s.{ ParseFailure, QueryParamDecoder }
import org.http4s.dsl.impl.ValidatingQueryParamDecoderMatcher

import scala.util.Try

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Currency] =
    QueryParamDecoder[String].emap(
      currency =>
        Try(Currency.fromString(currency)).toEither.leftMap(error => ParseFailure(error.getMessage, error.getMessage))
    )

  object FromQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("to")

}
