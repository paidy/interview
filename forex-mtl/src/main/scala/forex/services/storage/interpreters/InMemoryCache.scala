package forex.services.storage.interpreters

import cats.effect.Async
import cats.syntax.all._
import forex.domain.Rate
import forex.services.storage.Algebra
import scalacache.Entry
import scalacache.caffeine._
import scalacache.Mode
import com.github.benmanes.caffeine.cache.Caffeine
import forex.config.StorageConfig

class InMemoryCache[F[_]: Async](config: StorageConfig) extends Algebra[F] {

  implicit val mode: Mode[F] = scalacache.CatsEffect.modes.async[F]

  private val underlyingCaffeineCache = Caffeine
    .newBuilder()
    .expireAfterWrite(config.expireAfter.length, config.expireAfter.unit)
    .build[String, Entry[Rate]]()

  private val cache: CaffeineCache[Rate] = CaffeineCache(underlyingCaffeineCache)

  override def get(pair: Rate.Pair): F[Option[Rate]] = cache.doGet(pair)

  override def putAll(rates: List[Rate]): F[Unit] =
    rates.traverse(rate => cache.doPut(rate.pair, rate, Some(config.expireAfter))).void

  private implicit def pairToString(pair: Rate.Pair): String = pair.show
}
