package forex.components.scheduler.tasks

import cats.effect.{Concurrent, IO, IOApp, Timer}
import cats.implicits.toFlatMapOps
import forex.domain.Tasks
import forex.thirdparties.oneframae.OneFrameForexRatesHandler
import org.log4s.{Logger, getLogger}

import scala.concurrent.duration._

class TaskScheduler(task: Tasks, f: IO[Unit], frequency: FiniteDuration) extends IOApp .Simple {

  val logger: Logger = getLogger(getClass)

  private def schedule[F[_] : Concurrent : Timer](task: F[Unit], frequency: FiniteDuration) : F[Unit] = {
    Timer[F].sleep(frequency).flatMap(_ => task)
  }

  private[tasks] def scheduleJob(): Unit = {
    if (TasksLibrary.isTaskSpawned(this.task)) {
      logger.info("Job Already Spawned...")
    } else {
      schedule[IO](this.f, this.frequency)
      TasksLibrary.addSpawnedTask(task) match {
        case Some(_) => logger.info("Task Successfully Added To Task Library.")
        case None => logger.warn("Task Addition to Library Failed. Audit and fix the issue.")
      }
    }
  }

  override def run: IO[Unit] = {
    IO(scheduleJob())
  }
}

object TaskScheduler {
  def apply(task: Tasks, f: IO[Unit], frequency: FiniteDuration): TaskScheduler = {
    TaskScheduler(task, f, frequency)
  }

  def scheduleForexCacheRefreshJob = {
    TaskScheduler(Tasks.FOREX_JOB, OneFrameForexRatesHandler.handleCache(), 4.minutes)
  }
}

