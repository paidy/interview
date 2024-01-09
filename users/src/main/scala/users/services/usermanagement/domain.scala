package users.services.usermanagement

import scala.util.control.NoStackTrace

sealed trait Error extends Throwable with NoStackTrace

object Error:
  case object Exists extends Error
  case object NotFound extends Error
  case object Active extends Error
  case object Deleted extends Error
  case object Blocked extends Error
  case class System(underlying: Throwable) extends Error
