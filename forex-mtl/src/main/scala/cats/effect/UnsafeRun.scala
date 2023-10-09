package cats.effect

import scala.concurrent.Future
import unsafe.implicits.global


trait UnsafeRun[F[_]]{
  def unsafeToFuture[A](fa: F[A]): Future[A]
}


object UnsafeRun {

  def apply[F[_]](implicit F: UnsafeRun[F]): UnsafeRun[F] = F

  implicit object unsafeRunForCatsIO extends UnsafeRun[IO]{
    override def unsafeToFuture[A](io: IO[A]): Future[A] = io.unsafeToFuture()
  }
}