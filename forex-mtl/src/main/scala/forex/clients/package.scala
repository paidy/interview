package forex

package object clients {
  type RatesClient[F[_]] = rates.Algebra[F]
  final val RatesClient = rates.OneFrameClient
}
