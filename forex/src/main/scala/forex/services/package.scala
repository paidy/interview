package forex

package object services {

  type OneFrame[F[_]] = oneframe.Algebra[F]
  final val OneFrame = oneframe.Interpreters
  type OneFrameError = oneframe.Error
  final val OneFrameError = oneframe.Error

}
