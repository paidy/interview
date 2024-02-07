package forex.http.rates

import cats.implicits.toBifunctorOps
import forex.domain.Currency
import org.http4s.{ParseFailure, QueryParamDecoder}
import org.http4s.dsl.impl.ValidatingQueryParamDecoderMatcher

import scala.util.Try

object QueryParams {
//https://http4s.org/v1/docs/dsl.html#invalid-query-parameter-handling
  private[http] implicit val currencyQueryParam: QueryParamDecoder[Currency] =
    QueryParamDecoder[String]
      .emap(currencyStr => Try(Currency.fromString(currencyStr))
      .toEither
      .leftMap(t => ParseFailure(t.getMessage, t.getMessage)))

  object FromQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("to")

}
