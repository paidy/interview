package forex.interfaces.api.utils.marshalling

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import monix.eval._
import monix.execution._

import scala.concurrent._

trait MonixTaskSupport {

  implicit def taskMarshaller[T](
      implicit
      m: ToResponseMarshaller[Future[T]]
  ): ToResponseMarshaller[Task[T]] =
    Marshaller[Task[T], HttpResponse] { implicit ec ⇒ task ⇒
      val res = task.runAsync(Scheduler(ec))
      m(res)
    }

}

object MonixTaskSupport extends MonixTaskSupport
