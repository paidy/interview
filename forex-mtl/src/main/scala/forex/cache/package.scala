package forex

package object cache {
  type OneFrameCache[F[_]] = cache.Algebra[F]
  final val OneFrameCache = cache.interpreters.OneFrameCache
}
