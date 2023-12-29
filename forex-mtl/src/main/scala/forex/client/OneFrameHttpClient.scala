package forex.client

import cats.effect.Async
import cats.implicits._
import forex.config.OneFrameConfig
import forex.domain.OneFrameCurrencyInformation
import forex.domain.Rate.Pair
import io.circe.{Error => CError}
import sttp.client._
import sttp.client.circe.asJson

trait OneFrameClient[F[_]] {
  def getRates(pairs: Vector[Pair]): F[Response[Either[ResponseError[CError], List[OneFrameCurrencyInformation]]]]
}

class OneFrameHttpClient[F[_]: Async](
                                                                 oneFrameConfig: OneFrameConfig,
                                                                 implicit val backend: SttpBackend[Identity, Nothing, NothingT]
                                                               ) extends  OneFrameClient[F]{

  def getRates(pairs: Vector[Pair]): F[Response[Either[ResponseError[CError], List[OneFrameCurrencyInformation]]]] = {
    val param = pairs.map((pair: Pair) => "pair" -> s"${pair.from}${pair.to}")
    val url = uri"http://${oneFrameConfig.url}/rates?$param"
    println("herehere")
    println(url)

   val request = basicRequest
        .get(uri = url)
        .contentType("application/json")
        .header("token", "10dc303535874aeccc86a8251e6992f5")
        .response(asJson[List[OneFrameCurrencyInformation]])
        .send().pure[F]
    request
  }.recoverWith {
        case t =>
          println(s"t.getMessage = ${t.getMessage}")
          Async[F].raiseError(new Exception("Failed to get rates", t))
      }


//  override def getRate(pair: Pair): F[Response[Either[ResponseError[Error], OneFrameCurrencyInformation]]] = {
//    val param = pair.to.show + pair.from.show
////    pairs.map((pair: Pair) => "pair" -> s"${pair.from}${pair.to}")
//    val url = s"http://${oneFrameConfig.url}/rates?pair=$param"
//    println(s"url = ${url}")
//    Async[F].delay{
//      val request = basicRequest
//        .get(uri = uri"$url")
//        .contentType("application/json")
//        .header("token", "10dc303535874aeccc86a8251e6992f5")
//        .response(asJson[OneFrameCurrencyInformation])
//      println(s"requestHere = ${request.send()}")
//
//      request.send()
//    }.recoverWith {
//      case t => Async[F].raiseError(new Exception("Failed to get rates", t))
//    }
//  }
}

object OneFrameClient {
  def apply[F[_]: OneFrameClient]: OneFrameClient[F] = implicitly

  implicit def OneFrameHttpClient[F[_]: Async](config: OneFrameConfig)(
                                                                          implicit backend: SttpBackend[Identity, Nothing, NothingT]
                                                                        ): OneFrameClient[F] =
    new OneFrameHttpClient[F](config, backend)
}
