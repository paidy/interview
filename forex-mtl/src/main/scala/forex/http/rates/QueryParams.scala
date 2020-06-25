package forex.http.rates

import forex.domain.Currency
import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.QueryParamDecoderMatcher

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Option[Currency]] =
    QueryParamDecoder[String].map(Currency.fromString)

  object FromQueryParam extends QueryParamDecoderMatcher[Option[Currency]]("from")
  object ToQueryParam extends QueryParamDecoderMatcher[Option[Currency]]("to")

}
