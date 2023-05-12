package forex

package object services {
  type RatesService[F[_]] = rates.OneFrameAlgebra[F]
  final val RatesServices = rates.Interpreters
}
