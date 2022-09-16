package forex.services.rates.interpreters

import cats.effect.Concurrent
import cats.effect.concurrent.{Ref, Semaphore}
import cats.syntax.all._
import forex.domain.Currency.currencies
import forex.domain.Rate
import forex.domain.Rate.Pair
import forex.services.rates.errors.Error
import forex.services.rates.interpreters.OneFrameCached.{CachedRate, allCurrencyCombinations, ratesToCacheMap}
import forex.services.rates.{Algebra, BatchedAlgebra}

import java.time.{Duration, Instant}
import scala.math.Ordered.orderingToOrdered


class OneFrameCached[F[_]: Concurrent](
  private val batchService: BatchedAlgebra[F],
  private val cachedResults: Ref[F, Map[Pair, CachedRate]],
  private val semaphore: Semaphore[F]
) extends Algebra[F] {
  override def get(pair: Rate.Pair): F[Error Either Rate] = for {
    currentResults <- cachedResults.get
    currentValue = currentCachedValue(pair, currentResults)

    possibleRate <- currentValue.map(value => value.asRight[Error].pure).getOrElse({
      for {
        freshValue <- semaphore.withPermit(for {
          moreCurrentResults <- cachedResults.get
          recheckedValue = currentCachedValue(pair, moreCurrentResults)

          cachedOrFetchedValue <- recheckedValue.map(value => value.asRight[Error].pure).getOrElse({
            batchService.get(allCurrencyCombinations).flatMap {
              case Left(error) => error.asLeft[Rate].pure
              case Right(resp) => {
                val map = ratesToCacheMap(resp)
                cachedResults.set(map).flatMap(_ => map(pair).rate.asRight[Error].pure)
              }
            }
          })
        } yield cachedOrFetchedValue)
      } yield freshValue
    })
  } yield possibleRate

  def currentCachedValue(pair: Rate.Pair, cachedValues: Map[Pair, CachedRate]): Option[Rate] = {
    cachedValues.get(pair).filter(canReturnRate).map(_.rate)
  }

  def canReturnRate(possibleRate: CachedRate): Boolean = {
    possibleRate.cacheTime > Instant.now().minus(Duration.ofMinutes(5L))
  }
}

object OneFrameCached {
  case class CachedRate(cacheTime: Instant, rate: Rate)

  // This code assumes that the exchange rate of Currencies A->B is not necessarily
  // a mathematical inverse of the of the exchange rate of B->A
  val allCurrencyCombinations: Seq[Pair] = currencies.combinations(2).flatMap(currencyPair => {
    Seq(
      Pair(from = currencyPair.head, to = currencyPair.tail.head),
      Pair(from = currencyPair.tail.head, to = currencyPair.head)
    )
  }).toSeq

  def ratesToCacheMap(rates: Seq[Rate]): Map[Pair, CachedRate] = {
    rates.map(rate => rate.pair -> CachedRate(rate.timestamp.value.toInstant, rate)).toMap
  }
}
