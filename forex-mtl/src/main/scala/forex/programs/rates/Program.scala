package forex.programs.rates

import cats.effect.{Async, Sync, UnsafeRun}
import com.github.blemale.scaffeine.{AsyncCache, Scaffeine}
import forex.clients.RatesClient
import forex.model.config.ProgramConfig
import forex.model.domain.{Currency, Rate}
import forex.model.http.Converters._
import forex.model.http.Protocol._
import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import forex.model.errors.RateErrors.{OneFrameCallFailed, RateNotFound}
import scala.concurrent.Future


class Program[F[_] : Async : UnsafeRun](config: ProgramConfig, ratesClient: RatesClient[F])
  extends Algebra[F] with LazyLogging {

  private val cache: AsyncCache[Rate.Pair, Rate] = Scaffeine()
    .expireAfterWrite(config.cacheExpireTimeout)
    .buildAsync()

  private def updateCache(ratePair: Rate.Pair): F[Rate] = ratesClient
    .get(Currency.allCurrencyPairs, config.oneFrameToken)
    .map(_.map(_.asRate))
    .recoverWith { err =>
      logger.error(s"Failed to read rates data from One-Frame service", err)
      Sync[F].raiseError(OneFrameCallFailed(err))
    }
    .flatMap { ratesData =>
      logger.info(s"Got ${ratesData.size} rate records from One-Frame service")
      ratesData.foreach(rate => cache.put(rate.pair, Future.successful(rate)))
      ratesData.find(_.pair == ratePair).map(Sync[F].delay(_))
        .getOrElse(Sync[F].raiseError(RateNotFound(ratePair)))
    }

  override def get(from: Currency.Value, to: Currency.Value): F[GetApiResponse] = Async[F]
    .fromFuture(Sync[F].delay(cache
      .getFuture(Rate.Pair(from, to), pair => UnsafeRun[F].unsafeToFuture(updateCache(pair)))))
    .map { rate =>
      logger.info(s"Read from rates cache, got: $rate")
      rate.asGetApiResponse
    }
}


object Program {

  def apply[F[_] : Async : UnsafeRun](config: ProgramConfig, ratesClient: RatesClient[F]): Algebra[F] =
    new Program[F](config, ratesClient)
}
