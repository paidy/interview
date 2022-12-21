package forex

package object services {
  type RatesService[F[_]] = rates.Algebra[F]
  final val RatesServices = rates.Interpreters

  type HealthCheckService[F[_]] = healthcheck.Algebra[F]
  type CacheService[F[_]] = cache.Algebra[F]
}
