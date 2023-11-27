package forex.http.rates

import cats.data.ValidatedNel
import forex.domain.Currency
import org.http4s.{ ParseFailure, QueryParamDecoder }
import org.http4s.dsl.impl.QueryParamDecoderMatcher

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[ValidatedNel[ParseFailure, Currency]] =
    QueryParamDecoder[String].map(Currency.fromString)

  object FromQueryParam extends QueryParamDecoderMatcher[ValidatedNel[ParseFailure, Currency]]("from")
  object ToQueryParam extends QueryParamDecoderMatcher[ValidatedNel[ParseFailure, Currency]]("to")

}
