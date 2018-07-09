package forex

package object services {

  type OneForge[F[_]] = oneforge.Algebra[F]
  final val OneForge = oneforge.Interpreters
  type RatesCache[F[_]] = cache.Algebra[F]
  final val RatesCache = cache.Interpreters
  type OneForgeError = oneforge.Error
  final val OneForgeError = oneforge.Error

}
