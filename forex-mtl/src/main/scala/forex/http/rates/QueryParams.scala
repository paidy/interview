package forex.http.rates

import cats.implicits.toBifunctorOps
import forex.domain.Currency
import org.http4s.{ ParseFailure, QueryParamDecoder }
import org.http4s.dsl.impl.ValidatingQueryParamDecoderMatcher

import scala.util.Try

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Currency] =
    QueryParamDecoder[String].emap { cur =>
      Try(Currency.withValue(cur)).toEither.leftMap(err => ParseFailure(err.getMessage, err.getMessage))
    }

  object FromQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("to")

}
