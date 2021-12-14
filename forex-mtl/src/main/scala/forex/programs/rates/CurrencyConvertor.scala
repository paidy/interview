package forex.programs.rates

import forex.domain.{Price, Rate, Timestamp}
import scala.util.control.NonFatal

object CurrencyConvertor {

  def convertCurrency(pair: Rate.Pair): Rate = {
    val to = pair.to
    val from = pair.from
    try {
      val url: String = s"https://free.currconv.com/api/v7/convert?q=${from}_${to}&compact=ultra&apiKey=35f244be42e44cc209a1"
      val json: String = scala.io.Source.fromURL(url).mkString
      val convertedAmount: Double = json.split(":")(1).replaceAll("}", "").toDouble
      Rate(pair, Price(BigDecimal(convertedAmount)), Timestamp.now)
    } catch {
      case NonFatal(e) => throw new Exception(e.getMessage)
    }
  }
}
