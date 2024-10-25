package forex.thirdPartyApi.oneFrame

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import io.circe.parser.decode
import forex.domain.Rate
import forex.domain.Price
import forex.domain.Currency
import forex.domain.Timestamp
import scala.collection.immutable.HashMap
import sttp.client4.quick._
import sttp.model.Uri
import errors._
import forex.logging.Logger.logger

final case class RateSchema(from: String, to: String, bid: BigDecimal, ask: BigDecimal, price: BigDecimal, time_stamp: String)
final case class ErrorResponse(error: String)

object RateSchema {
  implicit final val RateDtoCodec: Codec[RateSchema] = deriveCodec
}

object ErrorResponse {
	implicit final val ApiErrorResponseCodec: Codec[ErrorResponse] = deriveCodec
}

class OneFrameApiClient {
  def getAll(): Either[OneFrameApiClientError, Map[Rate.Pair, Rate]] = {
		// Build URL and add currency pairs as query parameters 
		implicit var url: Uri = config.endpoint.get()

		this.getCurrencyPairs()
			.foreach((pair) => {
				url = url.addParam("pair", pair.from.toString() + pair.to.toString())
			})

		// Send request
		val response = quickRequest
			.get(url)
			.header("token", config.token.get())
			.send()
		
		// Convert response body to RateSchema
		val maybeRateSchemas = decode[List[RateSchema]](response.body).fold(
			_ => Left(decodeErrorResponse(response.body)),
			r => Right(r)
		)

		var rateMap = new HashMap[Rate.Pair, Rate]()

		maybeRateSchemas.fold(
			e => Left(e),
			rateSchemas => {
				for (rateSchema <- rateSchemas) {
					val ratePair = Rate.Pair(Currency.fromString(rateSchema.from), Currency.fromString(rateSchema.to))
					val rate = Rate(ratePair, Price(rateSchema.price), Timestamp.fromString(rateSchema.time_stamp))
					rateMap = rateMap + (ratePair -> rate)
				}
				Right(rateMap)
			}
		)
  }

  private def getCurrencyPairs(): Set[Rate.Pair] = {
		val allCurrencies: Set[(Currency, Int)] = Currency.cases().zipWithIndex
		val pairs: Set[Rate.Pair] = for {
  		(c1, i) <- allCurrencies
  		(c2, j) <- allCurrencies if i != j
		} yield (Rate.Pair(c1,c2))

		return pairs
  }

	private def decodeErrorResponse(response: String)(implicit url: Uri): OneFrameApiClientError = {
		decode[ErrorResponse](response).fold(
			_ => {
				logger.error(s"Could not decode JSON: ${response}")
				errors.OneFrameApiClientError.JsonDecodingError("Could not decode JSON")
			},
			r => {
				logger.error(s"Error from OneFrame API.\nURL: ${url}\nError: ${r.error}")
				errors.OneFrameApiClientError.RequestError(r.error)
			}
		)
	}
}