package forex.thirdparties.oneframae

import cats.effect.IO
import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import forex.components.CacheAPI
import forex.components.cache.redis.{ForexRedisHelper, Segments}
import forex.domain.Currency
import forex.programs.rates.ErrorCodes
import io.circe.{Json, ParsingFailure}
import io.circe.parser._
import requests.Response
import forex.programs.rates.errors._
import org.log4s.{Logger, getLogger}

object OneFrameForexRatesHandler {

  val logger: Logger = getLogger(getClass)

  def handleCache(): IO[Unit] = {
    populateCacheFromOneFrameService() match {
      case Right(_) => IO(logger.info("Successfully Refreshed Cache."))
      case Left(value) => IO(logger.warn(s"Cache Refresh Failed due to ${value.getMessage}. Have a quick audit and fix it."))
    }
  }

  private def populateCacheFromOneFrameService(): Either[Error, Boolean] = {
    val redis = CacheAPI.redis(Segments.forexRates)

    fetchAllExchangeRates match {
      case Right(value) =>
        value.foreach(json => {

          redis.put(ForexRedisHelper.getFormattedKey(json.hcursor.get[String]("from").toOption.get
            , json.hcursor.get[String]("to").toOption.get)
            , json.hcursor.get[BigDecimal]("price").toOption.get)
        })
        Option(true).toRight(Error.RateLookupFailed(ErrorCodes.cacheInitFailed, "Cache Init Failed. Aborting Service Start!"))
      case Left(value) => Option(value).toLeft(false)
    }
  }

  private def fetchAllExchangeRates: Either[Error, Vector[Json]] = {
    val queryParam = constructCurrencyCombinations
      .map(x => s"pair=$x")
      .reduce((x,y) => x + "&" + y)

    parse(callService(queryParam)) match {
      case Right(json) =>
        json.asArray match {
          case Some(array) => Option(array).toRight(Error.RateLookupFailed(ErrorCodes.cacheInitFailed, "Cache Init Failed. Aborting Service Start!"))
          case None => Option(Error.RateLookupFailed(ErrorCodes.fxRateLookUpFailed, "Cache Init Failed. Aborting Service Start!")).toLeft(Vector[Json]())
        }
      case Left(_) => Option(Error.RateLookupFailed(ErrorCodes.fxRateLookUpFailed, "Cache Init Failed. Aborting Service Start!")).toLeft(Vector[Json]())
    }
  }


  private def constructCurrencyCombinations : List[String] = {
    for {
      x <- Currency.supportedCurrencies
         y <- Currency.supportedCurrencies
          if (x != y) } yield {
      val pair = Currency.show.show(x) + Currency.show.show(y)
      pair
    }
  }

  private def callService(queryParam: String) : String = {
    val config = ConfigFactory.load()
    val appConfig = config.getConfig("app")
    val appConfigJson: Either[ParsingFailure, Json] = parse(appConfig.root().render(ConfigRenderOptions.concise()))
    appConfigJson match {
      case Right(value) => {
        val response: Response = requests.get("http://localhost:8080/rates?" + queryParam, headers = Map("token" -> value.hcursor.downField("oneframe").downField("token").as[String].toString))
        response.text()
      }
      case Left(_) => ""
    }


  }

}
