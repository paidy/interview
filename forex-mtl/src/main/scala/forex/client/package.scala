package forex

package object client {
  type OneFrameClient[F[_]] = client.Algebra[F]
  final val OneFrameClient = client.interpreters.OneFrameClient

}
