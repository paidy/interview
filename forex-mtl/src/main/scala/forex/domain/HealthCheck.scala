package forex.domain


object HealthCheck {

  sealed trait Status
  object Status {
    case object OK extends Status
    case object Unreachable extends Status
  }

  case class RedisStatus(value: Status)

  case class AppStatus(redis: RedisStatus)
}
