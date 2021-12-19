package forex.services.rates.interpreters

import forex.services.rates.Algebra
import cats.Applicative
import cats.syntax.applicative._
import cats.syntax.either._
import forex.domain.{Price, Rate, Timestamp}
import forex.services.rates.errors._



class OneFrameDummy[F[_]: Applicative] extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] =
    Rate(pair, Price(BigDecimal(100)), Timestamp.now).asRight[Error].pure[F]

}
/*
class OneFrameAdapter[F[_]: Applicative] extends Algebra[F] {
  override def get(pair: Rate.Pair): F[Either[Error, Rate]] = {
    import org.http4s.Method.GET
    import org.http4s.client._
    import org.http4s.client.dsl.io._
    import org.http4s.client.blaze._
    import org.http4s.Uri.uri
    import cats.effect.IO
    import io.circe.generic.auto._
    import fs2.Stream

    // Decode the response
    def helloClient(name: String): Stream[IO, Hello] = {
      // Encode a User request
      // todo change from config
      val req = GET(uri("http://localhost:8080/"), User(name).asJson)
      // Create a client
      BlazeClientBuilder[IO](ec).stream.flatMap { httpClient =>
        // Decode a Hello response
        Stream.eval(httpClient.expect(req)(jsonOf[IO, Hello]))
      }
    }

  }
}*/