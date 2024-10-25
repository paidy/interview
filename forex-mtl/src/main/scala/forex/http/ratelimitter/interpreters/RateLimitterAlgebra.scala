package forex.http.ratelimitter.interpreters

import forex.http.ratelimitter.RateLimiterSuccess
import forex.http.ratelimitter.error.RateLimitError


trait RateLimitterAlgebra[F[_]] {
  def isAllowed(token: String): F[RateLimitError Either RateLimiterSuccess]
  def increment(key: String): F[Unit]
}