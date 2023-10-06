package forex.programs.rates

import cats.data.OptionT
import cats.effect.{Async, Sync}
import cats.implicits._
import forex.cache.RatesCache
import forex.model.domain.{Currency, Rate}
import org.http4s.{Response, Status}
import org.http4s.dsl.Http4sDsl

import forex.model.http.Marshalling._
import forex.model.http.Converters._
import forex.model.http.Protocol._


class Program[F[_] : Async](ratesCache: RatesCache[F]) extends Algebra[F] with Http4sDsl[F] {

  override def get(from: Currency.Value, to: Currency.Value): F[Response[F]] = OptionT
    .some[F](Rate.Pair(from, to))
    .flatMap(ratePair => ratesCache.get(ratePair))
    .flatMap(rate => OptionT.liftF(Ok(rate.asGetApiResponse)))
    .getOrElseF(NotFound(s"No rate found for pair '$from' and '$to'"))
}

object Program {

  def apply[F[_] : Async](ratesCache: RatesCache[F]): Algebra[F] =
    new Program[F](ratesCache)
}
