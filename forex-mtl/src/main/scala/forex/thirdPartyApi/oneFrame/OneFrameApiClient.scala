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

final case class RateDto(from: String, to: String, bid: BigDecimal, ask: BigDecimal, price: BigDecimal, time_stamp: String)

object RateDto {
  implicit final val RateDtoCodec: Codec[RateDto] = deriveCodec
}

class OneFrameApiClient {
  def getAll(): Either[OneFrameApiClientError, Map[Rate.Pair, Rate]] = {
	// Build URL and add currency pairs as query parameters 
	var url: Uri = config.endpoint.get()
	this.getCurrencyPairs()
	.foreach((pair) => {
		url = url.addParam("pair", pair.from.toString() + pair.to.toString())
	})

	// Send request
	val response = quickRequest.get(url).header("token", config.token.get()).send()
		
	// Convert response body to RateDtos
	val maybeRateDtos = decode[List[RateDto]](response.body) match {
		case Left(_) => Left(errors.OneFrameApiClientError.JsonDecodingError("Error decoding JSON"))
		case Right(i) => Right(i)
	}

	var rateMap: Map[Rate.Pair, Rate] = new HashMap()

	maybeRateDtos.fold(
		e => Left(e),
		rateDtos => {
			for (rateDto <- rateDtos) {
				val ratePair = Rate.Pair(Currency.fromString(rateDto.from), Currency.fromString(rateDto.to))
				val rate = Rate(ratePair, Price(rateDto.price), Timestamp.fromString(rateDto.time_stamp))
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
}