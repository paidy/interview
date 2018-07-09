package forex.services.oneforge

import java.time.{Instant, OffsetDateTime, ZoneId}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import forex.domain.Currency.{AUD, GBP, JPY, USD}
import forex.domain.{Price, Rate, Timestamp}
import forex.services.oneforge.Error.{ApiException, UnparsableQuoteResponse, UnsupportedContentType}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import scala.concurrent.duration._
import scala.concurrent.Await

class OneForgeCallerHelperSpec extends WordSpec
  with Matchers
  with ScalaFutures
  with BeforeAndAfterAll {

  implicit val system = ActorSystem("test")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  override def afterAll(): Unit = {
    super.afterAll()
    Await.ready(system.terminate(), 10.seconds)
  }

  import OneForgeCallerHelper._

  "OneForgeCallerHelper" when {
    "decoding quotes" should {
      "return UnsupportedContentType error when content type was other than application/json" in {
        val response = HttpResponse(
          entity = HttpEntity.Strict(ContentTypes.`text/csv(UTF-8)`, ByteString.empty)
        )

        whenReady(decodeQuotes(response).failed) { e =>
          e shouldEqual UnsupportedContentType(ContentTypes.`text/csv(UTF-8)`.toString)
        }
      }

      "return UnparsableQuoteResponse error when wan unable to parse content" in {
        val response = HttpResponse(
          entity = HttpEntity.Strict(
            ContentTypes.`application/json`,
            ByteString("SOMETHING")
          )
        )

        whenReady(decodeQuotes(response).failed) { e =>
          e shouldBe an[UnparsableQuoteResponse]
        }
      }

      "return list of parsed OneForgeQuoteResponses" in {
        val response = HttpResponse(
          entity = HttpEntity.Strict(
            ContentTypes.`application/json`,
            ByteString(exampleResponse)
          )
        )

        whenReady(decodeQuotes(response)) { result =>
          result shouldEqual List(
            OneForgeQuoteResponse(
              "AUDUSD",
              0.79248,
              0.79251,
              0.792495,
              1502160793
            ),
            OneForgeQuoteResponse(
              "GBPJPY",
              144.368,
              144.375,
              144.3715,
              1502160794
            )
          )
        }
      }
    }
    "checking if response was successful" should {
      "return Successful when return code was 200" in {
        val response = HttpResponse(status = StatusCodes.OK)

        whenReady(checkIfSuccessful(response)) { _ =>
        }
      }

      "return ApiException when it was not" in {
        val response = HttpResponse(status = StatusCodes.Found)

        whenReady(checkIfSuccessful(response).failed) { e =>
          e shouldBe an[ApiException]
        }
      }

    }

    "converting quotes to rates" should {
      "return UnparsableQuoteResponse when was unable to parse one of the symbols to pair" in {
        val quotes = List(
          OneForgeQuoteResponse(
            "AUDUSD",
            0.79248,
            0.79251,
            0.792495,
            1502160793
          ),
          OneForgeQuoteResponse(
            "XYZUSD",
            0.79248,
            0.79251,
            0.792495,
            1502160793
          )
        )

        whenReady(quotesToRates(quotes).failed) { e =>
          e shouldBe an[UnparsableQuoteResponse]
        }
      }
      "return a set of rates from provided quotes" in {
        val quotes = List(
          OneForgeQuoteResponse(
            "AUDUSD",
            0.79248,
            0.79251,
            0.792495,
            1502160793
          ),
          OneForgeQuoteResponse(
            "GBPJPY",
            144.368,
            144.375,
            144.3715,
            1502160794
          )
        )

        val expectedResult = Set(
          Rate(
            Rate.Pair(AUD, USD),
            Price(BigDecimal(0.792495)),
            Timestamp(
              OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(1502160793),
                ZoneId.systemDefault()
              )
            )
          ),
          Rate(
            Rate.Pair(GBP, JPY),
            Price(BigDecimal(144.3715)),
            Timestamp(
              OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(1502160794),
                ZoneId.systemDefault()
              )
            )
          )
        )

        whenReady(quotesToRates(quotes)) { result =>
          result shouldEqual expectedResult
        }
      }
    }

    "creating uri" should {
      "fill template with pairs converted to query parameter and an api key" in {
        val mockedEndpointTemplate = "TEMPLATE%sTEMPLATE%s"
        val pairs = Set(Rate.Pair(USD, GBP), Rate.Pair(USD, JPY))
        val key = "somekey"

        createUri(mockedEndpointTemplate, pairs, key) shouldEqual "TEMPLATEUSDGBP,USDJPYTEMPLATEsomekey"
      }
    }

  }

  val exampleResponse =
    """[
      |     {
      |          "symbol": "AUDUSD",
      |          "price": 0.792495,
      |          "bid": 0.79248,
      |          "ask": 0.79251,
      |          "timestamp": 1502160793
      |     },
      |     {
      |          "symbol": "GBPJPY",
      |          "price": 144.3715,
      |          "bid": 144.368,
      |          "ask": 144.375,
      |          "timestamp": 1502160794
      |     }
      |]""".stripMargin
}
