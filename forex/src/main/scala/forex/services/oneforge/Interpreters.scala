package forex.services.oneforge

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import cats.syntax.either._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import forex.config.ForexConfig
import forex.domain._
import forex.services.oneforge.Error.System
import monix.eval.Task
import org.atnos.eff._
import org.atnos.eff.addon.monix.task._

import scala.collection.immutable.Set
import scala.concurrent.ExecutionContext


object Interpreters {
  def dummy[R](
                implicit
                m1: _task[R]
              ): Algebra[Eff[R, ?]] = new Dummy[R]

  def live[R](
               forexConfig: ForexConfig
             )(
               implicit m1: _task[R],
               system: ActorSystem,
               materializer: Materializer,
               ec: ExecutionContext
             ): Algebra[Eff[R, ?]] = new Live[R](forexConfig)
}

final class Dummy[R] private[oneforge](
                                        implicit
                                        m1: _task[R]
                                      ) extends Algebra[Eff[R, ?]] {
  override def get(
                    pairs: Set[Rate.Pair]
                  ): Eff[R, Error Either Set[Rate]] = {
    for {
      result <- fromTask(Task.now(
        pairs.map(pair => Rate(pair, Price(BigDecimal(100)), Timestamp.now))
      ))
    } yield Right(result)
  }
}


final class Live[R] private[oneforge](forexConfig: ForexConfig)(
  implicit m1: _task[R],
  system: ActorSystem,
  materializer: Materializer,
  ec: ExecutionContext
) extends Algebra[Eff[R, ?]] with ErrorAccumulatingCirceSupport {

  import OneForgeCallerHelper._

  override def get(pairs: Set[Rate.Pair]): Eff[R, Either[Error, Set[Rate]]] =
    fromTask(
      Task.deferFuture(
        for {
          response <- callOneForgeFor(pairs)
          _ <- checkIfSuccessful(response)
          decodedQuotes <- decodeQuotes(response)
          rates <- quotesToRates(decodedQuotes)
        } yield rates
      )
        .map(_.asRight[Error])
        .onErrorHandle {
          case e: Error => e.asLeft[Set[Rate]]
          case e: Throwable => System(e).asLeft[Set[Rate]]
        }
    )

  private def callOneForgeFor(pairs: Set[Rate.Pair]) =
    Http().singleRequest(
      HttpRequest(uri = createUri(
        forexConfig.quotesEndpointTemplate,
        pairs,
        forexConfig.apiKey
      ))
    )
}