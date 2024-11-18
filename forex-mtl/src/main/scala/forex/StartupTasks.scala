package forex

import cats.effect.{ExitCode, IO}
import forex.components.scheduler.tasks.TaskScheduler
import forex.thirdparties.oneframae.OneFrameForexRatesHandler

object StartupTasks {
  def process(): IO[ExitCode] = {
    TaskScheduler.scheduleForexCacheRefreshJob
    IO(OneFrameForexRatesHandler.handleCache()).as(ExitCode.Success)
  }
}
