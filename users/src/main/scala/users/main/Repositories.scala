package users.main

import cats.data.Reader
import cats.Applicative

import users.config.*
import users.persistence.repositories.*

object Repositories:

  val reader: Reader[Unit, Repositories] =
    Reader((_: Unit) => Repositories())

  val fromApplicationConfig: Reader[ApplicationConfig, Repositories] =
    reader.local[ApplicationConfig](_ => ())

final case class Repositories() {
  def userRepository[F[_]: Applicative]: UserRepository[F] = UserRepository.inMemory()
}
