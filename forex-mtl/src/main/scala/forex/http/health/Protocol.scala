package forex.http.health

import io.circe.Encoder
import io.circe.generic.semiauto._
import io.circe.generic.extras.semiauto.deriveUnwrappedEncoder
import forex.domain.HealthCheck._

object Protocol {

  implicit val statusEncoder: Encoder[Status] = Encoder.forProduct1("status")(_.toString)
  implicit val RedisStatusEncoder: Encoder[RedisStatus] = deriveUnwrappedEncoder[RedisStatus]
  implicit val appStatusEncoder: Encoder[AppStatus] = deriveEncoder
}
