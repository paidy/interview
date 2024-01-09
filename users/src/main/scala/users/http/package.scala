package users

import org.http4s.HttpRoutes

package object http:

  trait Routes[F[_]]:
    def routes: HttpRoutes[F]
