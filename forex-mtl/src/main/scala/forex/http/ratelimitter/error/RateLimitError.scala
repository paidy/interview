package forex.http.ratelimitter.error

trait RateLimitError
case class InvalidToken(message: String) extends RateLimitError
case class TokenExhausted(message: String) extends RateLimitError