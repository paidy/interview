package forex.cache

trait CustomCache[K, V] {
  def put(key: K, value: V): Unit
  def get(key: K): Option[V]
}
