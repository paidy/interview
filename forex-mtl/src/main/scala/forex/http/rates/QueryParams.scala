package forex.http.rates

import cats.implicits._
import forex.domain.Currency
import org.http4s.{ParseFailure, QueryParamDecoder}
import org.http4s.dsl.impl.ValidatingQueryParamDecoderMatcher

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Currency] =
    QueryParamDecoder[String].emap { s =>
      Currency.withNameEither(s).leftMap {
        err => ParseFailure("unable to parse argument as a Currency or not supported", err.getMessage())
      }
    }

  object FromQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("to")

}
