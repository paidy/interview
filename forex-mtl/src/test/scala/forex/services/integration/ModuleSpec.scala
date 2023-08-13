package forex.services.integration

import cats.effect.{ Blocker, IO }
import cats.effect.testing.scalatest.AsyncIOSpec
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.dimafeng.testcontainers.GenericContainer
import forex.config.{ ApplicationConfig, HttpConfig, ProviderConfig, StorageConfig }
import forex.domain.{ Currency, Price }
import org.http4s.implicits._
import org.http4s.{ Method, Request, Status }
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

import scala.concurrent.duration._

class ModuleSpec extends AsyncWordSpecLike with AsyncIOSpec with Matchers with EitherValues with TestContainerForAll {

  import forex.http.rates.Protocol._

  override val containerDef: GenericContainer.Def[GenericContainer] = GenericContainer.Def(
    "paidyinc/one-frame",
    exposedPorts = Seq(8080)
  )

  private def providerConfig(port: Int) =
    ProviderConfig(s"http://localhost:$port", "10dc303535874aeccc86a8251e6992f5")

  "Module" should {
    "return an error on wrong request" in {
      withContainers { container =>
        val config = ApplicationConfig(
          HttpConfig("localhost", 8080, 40.seconds),
          StorageConfig(3.minutes, 1000),
          providerConfig(container.mappedPort(8080))
        )
        (for {
          blocker <- Blocker.apply[IO]
          backend <- AsyncHttpClientFs2Backend.resource[IO](blocker)
        } yield backend).use { backend =>
          val module = new forex.Module[IO](config, backend)

          module.httpApp.run(Request(method = Method.GET, uri = uri"/rates/?from=UAH&to=UAH")).map { response =>
            response.status shouldBe Status.BadRequest
          }
        }
      }
    }

    "return rate for valid currencies" in {
      withContainers { container =>
        val config = ApplicationConfig(
          HttpConfig("localhost", 8080, 40.seconds),
          StorageConfig(3.minutes, 1000),
          providerConfig(container.mappedPort(8080))
        )
        (for {
          blocker <- Blocker.apply[IO]
          backend <- AsyncHttpClientFs2Backend.resource[IO](blocker)
        } yield backend).use { backend =>
          val module = new forex.Module[IO](config, backend)

          module.httpApp.run(Request(method = Method.GET, uri = uri"/rates/?from=USD&to=JPY")).flatMap { response =>
            response.status shouldBe Status.Ok

            response.as[GetApiResponse].map { data =>
              data.from shouldBe Currency.USD
              data.to shouldBe Currency.JPY
            }
          }
        }
      }
    }

    "return 1 for equal currencies without request" in {
      val config = ApplicationConfig(
        HttpConfig("localhost", 8080, 40.seconds),
        StorageConfig(3.minutes, 1000),
        providerConfig(0)
      )
      (for {
        blocker <- Blocker.apply[IO]
        backend <- AsyncHttpClientFs2Backend.resource[IO](blocker)
      } yield backend).use { backend =>
        val module = new forex.Module[IO](config, backend)

        module.httpApp.run(Request(method = Method.GET, uri = uri"/rates/?from=USD&to=USD")).flatMap { response =>
          response.status shouldBe Status.Ok

          response.as[GetApiResponse].map { data =>
            data.from shouldBe Currency.USD
            data.to shouldBe Currency.USD
            data.price shouldBe Price(1)
          }
        }
      }
    }
  }

}
