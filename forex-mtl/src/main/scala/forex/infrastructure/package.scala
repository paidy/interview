package forex

import forex.infrastructure.cache.RatesCacheCaffiene
import com.github.blemale.scaffeine.Scaffeine
import scala.concurrent.duration._
import forex.domain.Rate

package object infrastructure {
  private val scaffiene =
      Scaffeine()
        .recordStats()
        .expireAfterWrite(3.minute)
        .maximumSize(100)
        .build[String, Rate]()

  final val ratesCacheCaffiene = new RatesCacheCaffiene(scaffiene)
}
