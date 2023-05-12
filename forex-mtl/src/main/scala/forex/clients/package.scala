package forex

package object clients {
  type OneFrameClient[F[_]] = rates.OneFrameClientAlgebra[F]
  final val OneFrameClients = rates.Interpreters
}
