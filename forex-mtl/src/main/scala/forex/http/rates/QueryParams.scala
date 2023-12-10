package forex.http.rates

import forex.domain.Currency
import forex.programs.rates.errors.Error.InvalidCurrencyString
import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.QueryParamDecoderMatcher

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Either[InvalidCurrencyString, Currency]] =
    QueryParamDecoder[String].map(Currency.fromString)

  object FromQueryParam extends QueryParamDecoderMatcher[Either[InvalidCurrencyString, Currency]]("from")
  object ToQueryParam extends QueryParamDecoderMatcher[Either[InvalidCurrencyString, Currency]]("to")

}
