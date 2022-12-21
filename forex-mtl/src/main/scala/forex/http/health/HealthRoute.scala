package forex.http
package health

import cats.effect.Sync
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import forex.services.HealthCheckService


class HealthRoute[F[_]: Sync](healthCheck: HealthCheckService[F]) extends Http4sDsl[F]{

  import Protocol._

  private[http] val prefixPath = "/health_check"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root  =>
      Ok(healthCheck.status)
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
