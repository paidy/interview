package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.errors._
import forex.components._
import forex.components.cache.redis.Segments


class OneFrameDummy[F[_]: Applicative] extends Algebra[F] {

  private final val redisAPI: Cache = CacheAPI.redis(Segments.forexRates)

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    val formattedCacheKey: String = getFormattedCacheKey(pair)
    redisAPI.get(formattedCacheKey) match {
      case Right(value) =>  Rate(pair, Price(BigDecimal(value.toDouble)), Timestamp.now)
        .asRight[Error]
        .pure[F]
      case Left(value) => value
        .asLeft[Rate]
        .pure[F]
    }
  }

  private def getFormattedCacheKey(pair: Rate.Pair) : String  =  {
    pair.from.toString + "&" + pair.to.toString
  }
}
