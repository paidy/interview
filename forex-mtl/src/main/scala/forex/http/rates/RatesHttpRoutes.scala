package forex.http
package rates

import cats.data.NonEmptyList
import cats.effect.Sync
import cats.implicits.{catsSyntaxApplicativeError, catsSyntaxApply}
import cats.syntax.flatMap._
import forex.http.ratelimitter.error.{InvalidToken, TokenExhausted}
import forex.http.ratelimitter.interpreters.RateLimitterAlgebra
import forex.programs.RatesProgram
import forex.programs.rates.errors.Error.RateLookupFailed
import forex.programs.rates.{Protocol => RatesProgramProtocol}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{Header, HttpRoutes, Response}
import org.slf4j.LoggerFactory
import org.typelevel.ci.CIString
class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F], rateLimiter: F[RateLimitterAlgebra[F]]) extends Http4sDsl[F] {
  private val logger = LoggerFactory.getLogger(getClass)
  import Converters._
  import Protocol._
  import QueryParams._

  private[http] val prefixPath = "/rates"

  def limit(token: Option[NonEmptyList[Header.Raw]], request: F[Response[F]]): F[Response[F]] =
    token match {
      case Some(tokenHeader) =>
        val tokenValue = tokenHeader.head.value
        rateLimiter.flatMap { rateLimiter =>
          rateLimiter.isAllowed(tokenValue).flatMap {
            case Right(true) =>
              rateLimiter.increment(tokenValue) *> request
            case Left(InvalidToken(message)) =>
              Forbidden(message)
            case Left(TokenExhausted(message)) =>
              TooManyRequests(message)
            case Left(_) =>
              InternalServerError("Something went wrong")
          }
        }
      case None =>
        BadRequest("Token header is missing")
    }

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      limit(
        req.headers.get(CIString("Token")), {
          (from, to) match {
            case (Some(from), Some(to)) =>
              rates.get(RatesProgramProtocol.GetRatesRequest(from, to)).flatMap {
                case Left(RateLookupFailed(error)) => InternalServerError(error)
                case Right(rate)                   => Ok(rate.asGetApiResponse)
              }
            case (Some(_), _) =>
              BadRequest("Invalid currency to")
            case (_, Some(_)) =>
              BadRequest("Invalid currency from")
            case _ =>
              BadRequest("Invalid currencies")
          }
        }
      ).handleErrorWith { e =>
        logger.error(e.getMessage, e)
        InternalServerError("Something went wrong")
      }
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
