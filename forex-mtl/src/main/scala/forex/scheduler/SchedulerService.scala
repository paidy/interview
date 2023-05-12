package forex.scheduler

import cats.Monad
import cats.data.EitherT
import cats.effect.Timer
import com.typesafe.scalalogging.StrictLogging
import forex.clients.rates.OneFrameClientAlgebra
import forex.config.ApplicationConfig.CacheConfig
import forex.persistence.RatesRepository
import forex.persistence.entity.RateEntity
import fs2.Stream

class SchedulerService[F[_]: Monad: Timer](config: CacheConfig,
                                           repository: RatesRepository[F],
                                           oneFrameClient: OneFrameClientAlgebra[F])
    extends StrictLogging {

  private val updateProgram: F[Int] = (for {
    rates <- EitherT(oneFrameClient.getRates)
    entities = rates.map(ofr => RateEntity(ofr.from, ofr.to, ofr.price, ofr.timeStamp))
    count <- EitherT(repository.updateMany(entities))
  } yield count) valueOr { error =>
    logger.error(s"Can't fetch rates for One-Frame: ${error.logMsg}")
    0
  }

  val scheduler: Stream[F, Int] =
    Stream.awakeEvery[F](config.ttl).evalMap(_ => updateProgram).evalTap { count =>
      logger.info(s"Updated $count entries in cache")
      Monad[F].pure(count)
    }
}

object SchedulerService {

  def apply[F[_]: Monad: Timer](config: CacheConfig,
                                repository: RatesRepository[F],
                                oneFrameClient: OneFrameClientAlgebra[F]) =
    new SchedulerService[F](config, repository, oneFrameClient)

}
