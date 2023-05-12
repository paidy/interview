package forex.persistence.statement

import doobie._
import doobie.implicits.toSqlInterpolator
import forex.domain.Currency
import forex.persistence.connection.DoobieMeta
import forex.persistence.entity.RateEntity

object RateStatements extends DoobieMeta {

  private val table          = "rates_cache"
  private val fromField      = "\"from\""
  private val toField        = "\"to\""
  private val priceField     = "price"
  private val timestampField = "\"timestamp\""

  def readByFromAndTo(from: Currency, to: Currency): doobie.Query0[RateEntity] = {
    val fragment = fr"SELECT" ++ Fragment.const(s"$fromField, $toField, $priceField, $timestampField") ++
      fr"FROM" ++ Fragment.const(table) ++ fr"WHERE" ++
      Fragment.const(fromField) ++ fr"= $from AND" ++ Fragment.const(toField) ++ fr"= $to"

    fragment.query[RateEntity]
  }

  def updateMany(entities: List[RateEntity]): ConnectionIO[Int] = {
    val sql =
      s"""|INSERT INTO $table ($fromField, $toField, $priceField, $timestampField) VALUES (?, ?, ?, ?)
          | ON CONFLICT($fromField, $toField)
          | DO UPDATE SET
          | $priceField = excluded.price, $timestampField = excluded.timestamp
          """.stripMargin
    Update[RateEntity](sql).updateMany(entities)
  }

}
