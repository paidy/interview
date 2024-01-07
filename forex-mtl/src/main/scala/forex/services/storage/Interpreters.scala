package forex.services.storage

import cats.effect.Async
import forex.config.StorageConfig
import forex.services.storage.interpreters.InMemoryCache

object Interpreters {
  def inMemory[F[_]: Async](config: StorageConfig): Algebra[F] = new InMemoryCache[F](config)
}
