package forex.services.oneforge

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.Materializer
import cats.Show
import cats.instances.list._
import cats.syntax.traverse._
import cats.instances.either._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import forex.domain.Rate
import forex.services.oneforge.Error.{ApiException, UnparsableQuoteResponse, UnsupportedContentType}
import io.circe.ParsingFailure

import scala.concurrent.{ExecutionContext, Future}

object OneForgeCallerHelper extends ErrorAccumulatingCirceSupport {


  def createUri(quotesEndpointTemplate: String,
                pairs: Set[Rate.Pair],
                apiKey: String)(implicit ps: Show[Rate.Pair]): String =
    quotesEndpointTemplate.format(pairs.map(p => ps.show(p)).mkString(","), apiKey)


  def checkIfSuccessful(response: HttpResponse): Future[Unit] =
    if (response.status == StatusCodes.OK) {
      Future.successful(())
    } else {
      Future.failed(
        ApiException(
          response.status.intValue(),
          response.status.reason()
        )
      )
    }

  def decodeQuotes(response: HttpResponse
                  )(implicit
                    materializer: Materializer,
                    ec: ExecutionContext
                  ): Future[List[OneForgeQuoteResponse]] =
    Unmarshal(response.entity)
      .to[List[OneForgeQuoteResponse]]
      .recoverWith {
        case Unmarshaller.UnsupportedContentTypeException(_) =>
          Future.failed(
            UnsupportedContentType(response.entity.contentType.toString)
          )
        case ParsingFailure(message, _) =>
          Future.failed(
            UnparsableQuoteResponse(message)
          )
      }

  def quotesToRates(quotes: List[OneForgeQuoteResponse]
                   ): Future[Set[Rate]] =
    quotes.traverse(_.toRate).map(_.toSet) match {
      case Left(e) => Future.failed(e)
      case Right(rates) => Future.successful(rates)
    }
}
