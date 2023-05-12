package forex.persistence.connection

import doobie.Meta
import doobie.postgres.{ Instances, JavaTimeInstances }
import forex.domain.{ Currency, Price }
import forex.persistence.entity.RateEntity

trait DoobieMeta extends Instances with JavaTimeInstances {

  implicitly[doobie.Read[Price]]
  implicitly[doobie.Write[Price]]

  implicit val metaConfiguration: Meta[Currency] = Meta[String].imap[Currency](Currency.withValue)(_.value)

  implicitly[doobie.Read[RateEntity]]
  implicitly[doobie.Write[RateEntity]]

}
