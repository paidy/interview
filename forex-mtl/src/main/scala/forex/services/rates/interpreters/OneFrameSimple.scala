package forex.services.rates.interpreters

import cats.effect.{ ContextShift, IO }
import forex.domain.Rate
import forex.services.rates.errors.Error
import forex.services.rates.{ Algebra, BatchAlgebra }
import org.http4s.{ Header, Headers, Query, Request, Uri }
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.io._
import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.services.rates.errors.Error.OneFrameLookupFailed
import forex.services.rates.interpreters.Decoders._

import scala.concurrent.ExecutionContext

class OneFrameSimple[F[_]: Applicative](baseUri: Uri, token: String) extends Algebra[F] with BatchAlgebra {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    getBatch(Seq(pair)).map(_.head).pure

  override def getBatch(pairs: Seq[Rate.Pair]): Either[Error, Seq[Rate]] = createBatchIO(pairs).unsafeRunSync()

  private def createBatchIO(pairs: Seq[Rate.Pair]): IO[Error Either Seq[Rate]] = {
    val uriWithParam =
      baseUri.copy(query = Query.fromVector(pairs.map(p => ("pair", Some(s"${p.from}${p.to}"))).toVector))
    val req = Request[IO](method = GET, uri = uriWithParam, headers = Headers.of(Header("token", token)))
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .use(c => c.expect[Error Either Seq[Rate]](req))
      .attempt
      .map {
        case Left(t) => OneFrameLookupFailed(t.toString).asLeft[Seq[Rate]]
        case Right(r) =>
          r match {
            case Left(l)  => l.asLeft[Seq[Rate]]
            case Right(r) => r.asRight[Error]
          }
      }
  }

}
