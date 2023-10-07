package forex

import cats.Parallel
import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.typesafe.scalalogging.LazyLogging
import forex.clients.rates.OneFrameClient
import forex.model.config.ApplicationConfig
import forex.model.domain.{Currency, Price, Rate, Timestamp}
import io.circe.generic.extras.Configuration
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource
import cats.effect.std.Supervisor
import pureconfig.generic.auto._

import scala.concurrent.duration._
import cats.implicits._
import cats.syntax.parallel._
import forex.model.http.Protocol.GetApiResponse
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.{Method, Request, Uri}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext


class ForexIntegrationSpec extends AsyncFlatSpec with AsyncIOSpec with Matchers with LazyLogging{

  val appConfig: ApplicationConfig = ConfigSource
    .default.at("app").loadOrThrow[ApplicationConfig]

  val fakeRatePair: Rate.Pair = Rate.Pair(Currency.CAD, Currency.CHF)

  val serviceUri: Uri = Uri
    .fromString(s"http://${appConfig.http.host}:${appConfig.http.port}/rates")
    .fold(
      err => fail(s"Can't build OneFrame URI from config ${appConfig.http}", err),
      res => res)

  def callService(ratePair: Rate.Pair): IO[GetApiResponse] = {
    import forex.model.http.Marshalling._
    import forex.model.http.Protocol._

    val request = Request[IO](
      method = Method.GET,
      uri = serviceUri
        .withQueryParam("from", ratePair.from.toString)
        .withQueryParam("to", ratePair.to.toString)
    )

    BlazeClientBuilder[IO].resource
      .use(_.expect[GetApiResponse](request))
  }

  it should "One-Frame service should be up" in {
    val oneFrameClient = new OneFrameClient[IO](appConfig.oneFrameClient)

    oneFrameClient
      .get(Set(fakeRatePair), appConfig.oneFrameService.oneFrameTokens.head)
      .recoverWith { err =>
        logger.error("One-Frame service error", err)
        IO.raiseError(err)
      }
      .map { res =>
        logger.info("One-Frame service response: " + res)
        res should not be empty
      }
  }

  it should "serve should for multiple requests" in {

    val appStarted = new Application[IO]
      .stream()
      .interruptAfter(12.seconds)
      .compile.drain

    val test = IO.pure(())
      .flatMap(_ => IO.sleep(5.seconds))           // Give time to service for start up
      .map(_ => Currency.allCurrencyPairs.toList)  // Run test for all currency pairs
      .flatMap(_
        .map(pair => callService(pair)             // Do the service call
          .map { response =>
            logger.info(s"For pair $pair got response $response")

            response.from shouldEqual pair.from
            response.to shouldEqual pair.to
          })
        .sequence
      )

      Supervisor[IO](await = true)
        .use(supervisor => supervisor.supervise(appStarted).flatMap(_ => test))
        .map(_ => ())
  }
}
