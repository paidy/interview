package forex

import cats.data.Reader

package object config {

  type ApplicationConfigReader[A] =
    Reader[ApplicationConfig, A]

  def configure[A](
      c: ApplicationConfig
  )(
      implicit
      r: ApplicationConfigReader[A]
  ): A =
    r.run(c)

}
