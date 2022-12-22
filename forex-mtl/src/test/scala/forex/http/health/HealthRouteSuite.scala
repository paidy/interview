package forex.http
package health

import cats._
import cats.effect._
import org.scalacheck.Gen
import org.http4s._
import org.http4s.implicits._
import org.http4s.Method._
import forex.domain.HealthCheck.{AppStatus, RedisStatus, Status => HealthStatus}
import forex.services.HealthCheckService
import io.circe.syntax.EncoderOps


object HealthRouteSuite extends HttpSuite {

  import Protocol._

  implicit val showAppStatus: Show[AppStatus] = Show.show(appStatus => appStatus.asJson.noSpaces)
  val statusGen: Gen[HealthStatus] = Gen.oneOf(HealthStatus.Unreachable, HealthStatus.OK)
  val redisStatusGen: Gen[RedisStatus] = statusGen.map(RedisStatus)
  val appStatusGen: Gen[AppStatus] = redisStatusGen.map(AppStatus)

  def dummyHealthCheckService(dummyStatus: AppStatus): HealthCheckService[IO] =
    new HealthCheckService[IO] {
      override def status: IO[AppStatus] = IO.pure(dummyStatus)
    }

  test("GET health_check OK") {
    forall(appStatusGen) { appStatus =>
      val req: Request[IO] = Request[IO](GET, uri"/health_check")
      val routes = new HealthRoute[IO](dummyHealthCheckService(appStatus)).routes
      expectHttpBodyAndStatus(routes, req)(appStatus, Status.Ok)
    }
  }
}
