package forex.services.rates.interpreters

import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import cats.syntax.option._
import forex.domain.{ Currency, Rate }
import forex.services.rates.errors.Error
import forex.services.rates.{ errors, Algebra, BatchAlgebra }
import scalacache._
import scalacache.modes.sync._
import scalacache.caffeine.CaffeineCache

import scala.concurrent.duration._

class OneFrameCache[F[_]: Applicative](batchAlgebra: BatchAlgebra, refreshTime: Int) extends Algebra[F] {

  val ratesCache: Cache[Rate] = CaffeineCache[Rate]

  private val currencyPairs = for {
    from <- Currency.values
    to <- Currency.values
    if from != to
  } yield Rate.Pair(from, to)

  override def get(pair: Rate.Pair): F[Either[errors.Error, Rate]] = {

    val singleRate = ratesCache.get(pair)
    val validRate =
      if (singleRate.isDefined) singleRate.get.asRight[Error]
      else {
        ratesCache.removeAll()
        batchAlgebra.getBatch(currencyPairs) match {
          case Left(e) => e.asLeft[Rate]
          case Right(rates) =>
            rates.foreach(r => ratesCache.put(r.pair)(r, refreshTime.seconds.some))
            ratesCache.get(pair).get.asRight[Error]
        }
      }
    validRate.pure
  }
}
