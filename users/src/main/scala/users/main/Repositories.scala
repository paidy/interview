package users.main

import cats.data.*
import cats.implicits.*
import cats.Applicative

import users.config.*
import users.persistence.repositories.*

object Repositories:

  def reader[F[_]: Applicative]: ReaderT[F, Unit, Repositories[F]] =
    ReaderT((_: Unit) => Repositories().pure)

  def fromApplicationConfig[F[_]: Applicative]: ReaderT[F, ApplicationConfig, Repositories[F]] =
    reader[F].local[ApplicationConfig](_ => ())

final case class Repositories[F[_]: Applicative]():
  val userRepository: UserRepository[F] = UserRepository.inMemory()
