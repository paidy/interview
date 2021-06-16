package forex.cache.interpreters

import java.time.LocalDateTime

import cats.effect.{ Concurrent, Sync, Timer }
import com.github.benmanes.caffeine.cache.Caffeine
import forex.cache.Algebra
import forex.domain.{ Currency, Rate }
import fs2.Stream
import scalacache.{ get, Cache, Entry }
import scalacache.caffeine.CaffeineCache
import scalacache.CatsEffect.modes.async
import forex.util.ForexLogger
import cats.implicits._
import forex.client.OneFrameClient

import scala.concurrent.duration.FiniteDuration

class OneFrameCache[F[_]: Concurrent](oneFrameClient: OneFrameClient[F], scheduleTime: FiniteDuration)
    extends Algebra[F]
    with ForexLogger[F] {
  override implicit protected def sync: Sync[F] = implicitly[Sync[F]]

  private val oneFrameCaffeine =
    Caffeine.newBuilder().maximumSize(10000L).build[String, Entry[Rate]]
  implicit private val oneFrameCache: Cache[Rate] = CaffeineCache(oneFrameCaffeine)

  private val allPairs: Vector[Rate.Pair] = Currency.allPairs.map(Rate.Pair.tupled)

  override def getRate(key: String): F[Option[Rate]] = get[F, Rate](key)

  override def scheduleCacheRefresh()(implicit timer: Timer[F]): Stream[F, Unit] =
    Stream.eval(populateCache()) >> Stream.awakeEvery[F](scheduleTime) >> Stream.eval(populateCache())

  private def populateCache(): F[Unit] =
    for {
      freshRates <- oneFrameClient.getRates(allPairs)
      rates = freshRates match {
        case Right(rates) => rates
        case Left(_) =>
          Logger.error(s"Unable to fetch dates from client")
          List.empty[Rate]
      }
      _ <- if (rates.nonEmpty) updateCache(rates) else {}.pure[F]
    } yield {}

  private def updateCache(rates: List[Rate]): F[Unit] =
    for {
      _ <- oneFrameCache.removeAll().void
      _ <- rates.map(rate => oneFrameCache.put(rate.pair.generateKey)(rate)).sequence
      _ <- Logger.info(s"Rates updated successfully at ${LocalDateTime.now()} with size ${rates.size}")
    } yield ()

}

object OneFrameCache {
  def apply[F[_]: Concurrent](oneFrameClient: OneFrameClient[F], scheduleTime: FiniteDuration): Algebra[F] =
    new OneFrameCache(oneFrameClient, scheduleTime)
}
