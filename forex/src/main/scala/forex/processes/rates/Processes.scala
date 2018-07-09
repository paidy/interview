package forex.processes.rates

import cats.syntax.either._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.{Applicative, Monad}
import forex.domain._
import forex.processes.rates.messages.Error.CurrentRateNotAvailable
import forex.services._

import scala.concurrent.duration.FiniteDuration

object Processes {
  def apply[F[_]]: Processes[F] =
    new Processes[F] {}
}

trait Processes[F[_]] {
  import converters._
  import messages._

  def updateRates(supportedPairs: Set[Rate.Pair])(
    implicit
    M: Monad[F],
    oneForge: OneForge[F],
    ratesCache: RatesCache[F]
  ): F[Either[Error, Unit]] =
    for {
      fetched <- oneForge.get(supportedPairs)
      result <- store(fetched)
    } yield result

  private def store(result: Either[OneForgeError, Set[Rate]]
                   )(implicit
                     ratesCache: RatesCache[F],
                     A: Applicative[F]): F[Either[Error, Unit]] =
    result match {
      case Right(newRates) => ratesCache.store(newRates).map(_.asRight[Error])
      case Left(error) => A.pure(toProcessError(error).asLeft[Unit])
    }

  def get(
      request: GetRequest,
      maxRateAge: FiniteDuration
         )(implicit
           ratesCache: RatesCache[F],
           M: Monad[F]
         ): F[Error Either Rate] =
    for {
      rate <- ratesCache.get(Rate.Pair(request.from, request.to))
      valid <- validate(rate, maxRateAge)
    } yield valid


  private def validate(maybeRate: Option[Rate],
                       maxRateAge: FiniteDuration
                      )(implicit
                        A: Applicative[F]): F[Either[Error, Rate]] =
    maybeRate match {
      case None => A.pure(CurrentRateNotAvailable.asLeft[Rate])
      case Some(rate) => A.pure(checkIfNotTooOld(rate, maxRateAge))
    }


  private def checkIfNotTooOld(rate: Rate,
                               maxRateAge: FiniteDuration
                              ): Either[Error, Rate] =
    if(rate.timestamp.isNotOlderThan(maxRateAge)) {
      rate.asRight[Error]
    } else {
      CurrentRateNotAvailable.asLeft[Rate]
    }
}