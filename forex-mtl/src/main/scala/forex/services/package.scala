package forex

package object services {
  type RatesService[F[_]]   = rates.Algebra[F]
  type StorageService[F[_]] = storage.Algebra[F]
  final val RatesServices  = rates.Interpreters
  final val StorageService = storage.Interpreters
}
