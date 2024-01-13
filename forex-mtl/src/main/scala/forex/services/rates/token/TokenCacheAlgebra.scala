package forex.services.rates.token


case class Tokens(map: Map[String, Int])
trait TokenCacheAlgebra[F[_]] {
  def getToken(): F[Option[String]]
  def incrementUsage(token: String): F[String]
}
