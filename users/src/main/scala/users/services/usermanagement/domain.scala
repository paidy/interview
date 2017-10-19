package users.services.usermanagement

import scala.util.control.NoStackTrace

sealed trait Error extends Throwable with NoStackTrace
object Error {
  final case object Exists extends Error
  final case object NotFound extends Error
  final case object Active extends Error
  final case object Deleted extends Error
  final case object Blocked extends Error
  final case class System(underlying: Throwable) extends Error
}
