package forex.services.rates

import forex.domain.{ Currency, Price, Rate, Timestamp }

import cats.implicits._
import java.time.{ Instant, OffsetDateTime }
import scala.util.Try

package interpreters {

  import cats.Applicative

  import java.time.ZoneId

  case class OneFrameResponse(from: Currency, to: Currency, price: Double, timeStamp: String) {
    def toRate[F[_]: Applicative]: F[errors.Error Either Rate] =
      Try(toRateUnsafe).toEither
        .leftMap(e => errors.Error.ParseFailure(s"Failed to convert response to Rate: ${e.getMessage}"): errors.Error)
        .pure[F]

    def toRateUnsafe: Rate = {
      val parsedTime = OffsetDateTime.ofInstant(Instant.parse(timeStamp), ZoneId.systemDefault())
      Rate(Rate.Pair(from, to), Price(price), Timestamp(parsedTime))
    }
  }

  object Implicits {
    implicit class OneFrameResponseOps(private val data: List[OneFrameResponse]) extends AnyVal {
      def toRates[F[_]: Applicative]: F[errors.Error Either List[Rate]] =
        Try {
          data.map(_.toRateUnsafe)
        }.toEither
          .leftMap(e => errors.Error.ParseFailure(s"Failed to convert response to Rate: ${e.getMessage}"): errors.Error)
          .pure[F]
    }
  }
}
