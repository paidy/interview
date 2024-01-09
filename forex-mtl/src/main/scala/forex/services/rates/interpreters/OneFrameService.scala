package forex.services.rates.interpreters

import cats.effect.{ Concurrent, Timer }
import cats.Applicative
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import forex.domain._
import forex.client.OneFrameClient
import forex.config.{ CacheConfig, SchedulerConfig }
import forex.services.rates.Algebra
import forex.services.rates.errors.Error.OneFrameLookupFailed
import fs2.Stream
import forex.services.rates.errors._
import scalacache.Cache
import scalacache.modes.sync.mode

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.duration.DurationInt

class OneFrameService[F[_]: Concurrent](oneFrameClient: OneFrameClient[F],
                                        rateCache: Cache[Rate],
                                        cacheConfig: CacheConfig,
                                        schedulerConfig: SchedulerConfig)
    extends Algebra[F]
    with LazyLogging {

  private val allPairs: Vector[Rate.Pair] = Currency.allPairs.map(Rate.Pair.tupled)

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    for {
      rate <- rateCache.get(pair.key) match {
               case Some(rate) => rate.asRight[Error].pure[F]
               case _ =>
                 logger.error("Unable to fetch rate from cache")
                 OneFrameLookupFailed("Cache not updated. Please contact admin.").asLeft[Rate].pure[F]
             }
    } yield rate

  private def populateCache(): F[Unit] =
    for {
      freshRates <- oneFrameClient.getRates(allPairs)
      rates = freshRates match {
        case Right(rates) => rates
        case Left(error) =>
          logger.error("API returned empty list when populating cache.", error)
          List.empty[OneFrameCurrencyInformation]
      }
      _ <- if (rates.nonEmpty) updateCache(rates) else Applicative[F].unit
    } yield {}

  override def scheduleCacheRefresh()(implicit timer: Timer[F]): Stream[F, Unit] =
    Stream.eval(populateCache()) >> Stream.awakeEvery[F](schedulerConfig.oneFrameRefresh.minutes) >> Stream.eval(
      populateCache()
    )

  private def updateCache(rates: List[OneFrameCurrencyInformation]): F[Unit] = {
    logger.info("Updating cache with latest values.")
    Applicative[F]
      .pure(rates.map { rate =>
        val currentRate = Rate(
          Rate.Pair(Currency.fromString(rate.from), Currency.fromString(rate.to)),
          Price.apply(rate.price),
          Timestamp(OffsetDateTime.parse(rate.time_stamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        )
        rateCache.put(currentRate.pair.key)(currentRate, cacheConfig.oneFrameExpiry.minutes.some)
      })
      .void
  }
}
