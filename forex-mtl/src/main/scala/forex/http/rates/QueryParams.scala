package forex.http.rates

import forex.domain.Currency
import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.ValidatingQueryParamDecoderMatcher
import org.http4s.ParseFailure

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Currency] =
    QueryParamDecoder[String].emap((q) => {
      Currency.tryFromString(q) match {
        case None =>  Left(ParseFailure(s"$q is not a valid currency.", s"$q is not a valid currency."))
        case Some(value) => Right(value)
      }
    }
  )

  object FromQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("to")

}
