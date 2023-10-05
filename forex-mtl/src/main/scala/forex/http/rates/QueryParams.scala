package forex.http.rates

import forex.domain.Currency
import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.QueryParamDecoderMatcher

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Currency.Value] =
    QueryParamDecoder[String].map(Currency.fromString(_).get)


  object FromQueryParam extends QueryParamDecoderMatcher[Currency.Value]("from")
  object ToQueryParam extends QueryParamDecoderMatcher[Currency.Value]("to")

}
