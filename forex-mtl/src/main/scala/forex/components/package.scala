package forex

import forex.components.cache.Algebra

package object components {
  type Cache = Algebra
  final val CacheAPI = cache.Protocol
}
