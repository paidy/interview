package forex.services.rates

import cats.Applicative
import interpreters._
import org.http4s.Uri

object Interpreters {
  def dummy[F[_]: Applicative](): Algebra[F] = new OneFrameDummy[F]()
  def simple[F[_]: Applicative](uri: Uri, token: String): Algebra[F] = new OneFrameSimple[F](uri, token)

}
