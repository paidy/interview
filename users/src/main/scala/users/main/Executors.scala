package users.main

import java.util.concurrent.ForkJoinPool

import cats.*
import cats.data.*
import cats.implicits.*

import scala.concurrent.ExecutionContext

import users.config.*

object Executors:
  def reader[F[_]: Applicative]: ReaderT[F, ExecutorsConfig, Executors] = ReaderT(Executors.apply(_).pure)

  def fromApplicationConfig[F[_]: Applicative]: ReaderT[F, ApplicationConfig, Executors] =
    reader[F].local[ApplicationConfig](_.executors)

final case class Executors(config: ExecutorsConfig):

  final val serviceExecutor: ExecutionContext =
    ExecutionContext.fromExecutor(new ForkJoinPool(config.services.parallellism))
