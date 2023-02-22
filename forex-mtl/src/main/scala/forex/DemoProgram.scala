package forex

import cats.data.NonEmptyList
import cats.effect._
import cats.effect.concurrent.Semaphore
import forex.domain._
//import forex.programs.rates.Program
import forex.programs.rates.BlockingProgram
import forex.programs.rates.Protocol.GetRatesRequest
import forex.services.{CacheService, RatesService}
import forex.services.rates.errors
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.syntax.all._

import java.time.OffsetDateTime
import cats.effect.concurrent.Ref

import scala.concurrent.duration._



object DemoProgram extends IOApp {

  implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  def fakeRateService(rate: Rate): RatesService[IO] =
    new RatesService[IO] {
      override def get(pair: Rate.Pair): IO[Either[errors.Error, Rate]] = IO.pure(Right(rate))

      override def getMany(pairs: NonEmptyList[Rate.Pair]): IO[Either[errors.Error, NonEmptyList[Rate]]] = {
        Logger[IO].info("Call Rate Service!") *>
          IO.sleep(500.millis) *>
        IO.pure(Right(NonEmptyList.fromListUnsafe(rate :: Nil)))
      }
    }

  def fakeCacheService(refMap: Ref[IO, Map[Rate.Pair, Rate]]): CacheService[IO] =
    new CacheService[IO] {

      override def get(pair: Rate.Pair): IO[Option[Rate]] = refMap.get.map(_.get(pair))

      override def set(pair: Rate.Pair, rate: Rate): IO[Boolean] =
        for {
          map <- refMap.get
          _ <- refMap.set(map + (pair -> rate))
        } yield true

      override def setMany(pairRates: Map[Rate.Pair, Rate]): IO[Boolean] =
        for {
          map <- refMap.get
          _ <- refMap.set(map ++ pairRates)
        } yield true
    }


  val requests = NonEmptyList.of(
    GetRatesRequest(Currency.AUD, Currency.CAD),
    GetRatesRequest(Currency.AUD, Currency.CAD),
    GetRatesRequest(Currency.AUD, Currency.CAD)
  )
  val program =
    for {
      s <- Semaphore[IO](1)
      ref <- Ref.of[IO, Map[Rate.Pair, Rate]](Map.empty)
      _ <- requests.map{ req =>
        BlockingProgram[IO](
          fakeRateService(
            Rate(
              Rate.Pair(Currency.AUD, Currency.CAD),
              Price(BigDecimal.apply(1)),
              Timestamp(OffsetDateTime.MIN))
          ),
          fakeCacheService(ref),
          s
        ).get(req).flatMap{
          case Right(v) => Logger[IO].info(s"Success with $v")
          case Left(e) => Logger[IO].info(s"Failed with $e")
        }
      }.parSequence
    } yield ExitCode.Success

  override def run(
    args: List[String]): IO[ExitCode] = program

}
