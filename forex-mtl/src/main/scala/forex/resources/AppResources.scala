package forex.resources

import org.http4s.client.Client
import org.http4s.client.blaze._
import cats.effect.ConcurrentEffect
import forex.config.{ApplicationConfig, HttpClientConfig}
import fs2.Stream

import scala.concurrent.ExecutionContext


sealed abstract class AppResources[F[_]](
  val client: Client[F]
)

object AppResources {

  def stream[F[_]: ConcurrentEffect](
    config: ApplicationConfig,
    ec: ExecutionContext
  ): Stream[F, AppResources[F]] = {
    def mkHttpClient(config: HttpClientConfig, ec: ExecutionContext) =
      BlazeClientBuilder[F](ec)
        .withResponseHeaderTimeout(config.timeout)
        .withIdleTimeout(config.idleTimePool)
        .resource
    Stream.resource(
      mkHttpClient(config.httpClient, ec).map(new AppResources[F](_) {})
    )
  }
}
