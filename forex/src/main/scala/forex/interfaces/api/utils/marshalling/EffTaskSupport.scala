package forex.interfaces.api.utils.marshalling

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import monix.eval._
import org.atnos.eff._
import shapeless.Lazy

trait EffTaskSupport extends MonixTaskSupport {

  implicit def effMarshaller[S, T](
      implicit
      run: Eff[S, T] ⇒ Task[T],
      m: Lazy[ToResponseMarshaller[Task[T]]]
  ): ToResponseMarshaller[Eff[S, T]] =
    Marshaller[Eff[S, T], HttpResponse] { implicit ec ⇒ eff ⇒
      val res = run(eff)
      m.value(res)
    }

}

object EffTaskSupport extends EffTaskSupport
