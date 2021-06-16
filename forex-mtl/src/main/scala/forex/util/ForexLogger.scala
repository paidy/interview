package forex.util
import cats.effect.Sync
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger
trait ForexLogger[F[_]] {
  implicit protected def sync: Sync[F]
  protected val Logger: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]
}
