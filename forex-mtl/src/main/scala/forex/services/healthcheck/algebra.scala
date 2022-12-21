package forex.services.healthcheck

import forex.domain.HealthCheck._

trait Algebra[F[_]] {
  def status: F[AppStatus]

}
