package forex

package object services {
  type RatesService[F[_]] = rates.Algebra[F]
  final val RatesServices = rates.Interpreters

  type CacheService = cache.Algebra
  final val CacheServices = cache.Interpreters
}
