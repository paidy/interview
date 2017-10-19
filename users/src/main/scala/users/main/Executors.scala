package users.main

import cats.data.Reader

import users.config._

import java.util.concurrent.ForkJoinPool
import scala.concurrent.ExecutionContext

object Executors {
  val reader: Reader[ExecutorsConfig, Executors] =
    Reader(Executors.apply)

  val fromApplicationConfig: Reader[ApplicationConfig, Executors] =
    reader.local[ApplicationConfig](_.executors)
}

final case class Executors(
    config: ExecutorsConfig
) {

  final val serviceExecutor: ExecutionContext =
    ExecutionContext.fromExecutor(new ForkJoinPool(config.services.parallellism))

}
