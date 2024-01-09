package forex.http.rates

import cats.implicits._
import forex.domain.Currency
import org.http4s.{ ParseFailure, QueryParamDecoder }
import org.http4s.dsl.io.ValidatingQueryParamDecoderMatcher

import scala.util.Try

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Currency] =
    QueryParamDecoder[String].emapValidatedNel { value =>
      Try(Currency.fromString(value)).toValidated
        .leftMap(_ => ParseFailure(value, "Wrong parameter value"))
        .toValidatedNel
    }

  object FromQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("to")

}
