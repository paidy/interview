package forex.persistence

import forex.programs.rates.errors.ForexError
import forex.programs.rates.errors.ForexError.DatabaseError

import java.sql.SQLException
import scala.util.control.NonFatal

trait ExceptionHandler {
  def handleException(ex: Throwable): ForexError
}

class DatabaseExceptionHandler extends ExceptionHandler {

  def handleException(ex: Throwable): ForexError =
    ex match {
      case ex: SQLException =>
        DatabaseError(sqlExceptionMessage(ex))
      case NonFatal(ex) =>
        DatabaseError(ex.getMessage)
      case fatal =>
        throw fatal
    }

  private def sqlExceptionMessage(ex: SQLException) =
    s"\n SQL state: ${ex.getSQLState}\n code: ${ex.getErrorCode}\n message: ${ex.getMessage}"
}
