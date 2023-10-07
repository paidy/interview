package forex


package object cache {
  type RatesCache[F[_]] = rates.Algebra[F]
  final val RatesCache = rates.RatesCache
}
