package forex.processes.rates

import cats.syntax.either._
import cats.syntax.functor._
import cats.{Applicative, Functor}
import forex.domain._
import forex.processes.rates.messages.Error.CurrentRateNotAvailable
import forex.services._

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.FiniteDuration

object Processes {
  def apply[F[_]]: Processes[F] =
    new Processes[F] {}
}

trait Processes[F[_]] {
  import converters._
  import messages._

  private[rates] val rates: TrieMap[Rate.Pair, Rate] = TrieMap.empty[Rate.Pair, Rate]

  def updateRates(supportedPairs: Set[Rate.Pair])(
    implicit
    M: Functor[F],
    oneForge: OneForge[F]
  ): F[Either[Error, Unit]] =
    for {
      result <- oneForge.get(supportedPairs)
    } yield store(result)

  private def store(result: Either[OneForgeError, Set[Rate]]
                   ): Either[Error, Unit] =
    result match {
      case Right(newRates) => newRates
        .foreach(newRate => rates.update(newRate.pair, newRate))
        .asRight[Error]
      case Left(error) => toProcessError(error).asLeft[Unit]
    }

  def get(
      request: GetRequest,
      maxRateAge: FiniteDuration
  )(
      implicit
      M: Applicative[F],
  ): F[Error Either Rate] = M.pure{
    for {
      rate <- Either.fromOption(rates.get(Rate.Pair(request.from, request.to)), CurrentRateNotAvailable)
      _ <- checkIfNotTooOld(rate, maxRateAge)
    } yield rate
  }

  private def checkIfNotTooOld(rate: Rate, maxRateAge: FiniteDuration): Either[Error, Unit] =
    if(rate.timestamp.isNotOlderThan(maxRateAge)) {
      ().asRight[Error]
    } else {
      CurrentRateNotAvailable.asLeft[Unit]
    }
}