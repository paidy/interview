package forex.main

import forex.config.ApplicationConfig
import org.atnos.eff.ConcurrentHashMapCache
import org.zalando.grafter.macros.readerOf

@readerOf[ApplicationConfig]
case class Caches() {

  final val hashMapRatesCache = ConcurrentHashMapCache()
}
