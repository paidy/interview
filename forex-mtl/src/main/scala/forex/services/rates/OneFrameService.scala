package forex.services.rates

import fs2.Stream
import cats.effect.Sync
import cats.effect.kernel.Async
import com.typesafe.scalalogging.LazyLogging
import forex.cache.RatesCache
import forex.clients.RatesClient
import cats.implicits._
import forex.model.config.OneFrameServiceConfig
import forex.model.http.Converters._
import forex.model.domain.{Currency, Rate}


class OneFrameService[F[_] : Async](
                                       config: OneFrameServiceConfig,
                                       ratesClient: RatesClient[F],
                                       ratesCache: RatesCache[F]
                                     ) extends Algebra[F] with LazyLogging {

  private def doRefresh(token: String): F[Unit] = Sync[F].delay(())
    .flatMap { _ =>
      ratesClient.get(Currency.allCurrencyPairs, token)
        .flatMap(apiRates => ratesCache.update(apiRates.map(_.asRate)))
    }
    .recover { err =>
      logger.error(s"Failed to process rates refresh for token '$token'", err)
    }

  val ratesRefresh: Stream[F, Unit] = Stream
    .evalSeq(Sync[F].delay(config.oneFrameTokens))
    .repeat                                         // Will cycle over all available tokens
    .metered(config.ratesRefreshTimeout)
    .map(token => (token, System.currentTimeMillis()))
    .map { case (token, tStart) =>
      logger.info(s"New iteration of rates refresh started at $tStart with token '$token'")
      (token, tStart)
    }
    .evalMap {  case (token, tStart) => doRefresh(token).map(_ => tStart) }
    .map { tStart =>
      logger.info(s"Iteration of rates refresh done, work time ${System.currentTimeMillis() - tStart} mills")
    }
}

object OneFrameService {

  def apply[F[_] : Async](
                             config: OneFrameServiceConfig, ratesClient: RatesClient[F], ratesCache: RatesCache[F]
                           ): Algebra[F] = new OneFrameService[F](config, ratesClient, ratesCache)
}
