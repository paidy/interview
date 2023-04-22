package forex.services.rates.interpreters

import cats.effect.Sync
import cats.implicits.toFunctorOps
import forex.domain.model.Rate
import forex.http.external.oneframe.OneFrameClient
import forex.services.rates.errors

import scala.collection.mutable
import scala.jdk.CollectionConverters.ConcurrentMapHasAsScala

class OneFrameImplOnMemory[F[_]: Sync](client: OneFrameClient[F]) extends OneFrameImpl[F](client) {
  override def get(pair: Rate.Pair): F[Either[errors.Error, Rate]] =
    Cache.concurrentHashMap.get(pair) match {
      case Some(rate) => Sync[F].delay(Right(rate))
      case None =>
        super.get(pair).map {
          case Right(rate) =>
            Cache.concurrentHashMap.put(pair, rate)
            Right(rate)
          case Left(error) => Left(error)
        }
    }
}

object Cache {
  // TODO: remove from cache after 1 minute and a half.
  val concurrentHashMap: mutable.Map[Rate.Pair, Rate] =
    new java.util.concurrent.ConcurrentHashMap[Rate.Pair, Rate]().asScala
}
