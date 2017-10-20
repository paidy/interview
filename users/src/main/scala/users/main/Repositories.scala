package users.main

import cats.data.Reader

import users.config._
import users.persistence.repositories._

object Repositories {
  val reader: Reader[Unit, Repositories] =
    Reader((_: Unit) ⇒ Repositories())

  val fromApplicationConfig: Reader[ApplicationConfig, Repositories] =
    reader.local[ApplicationConfig](_ ⇒ ())
}

final case class Repositories() {

  final val userRepository: UserRepository =
    UserRepository.inMemory()

}
