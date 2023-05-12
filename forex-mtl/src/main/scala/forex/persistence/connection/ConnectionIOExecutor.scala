/*
 * Copyright (c) TRANZZO LTD - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package forex.persistence.connection

import cats.effect.Bracket
import cats.implicits._
import doobie._
import doobie.implicits._
import forex.persistence.ExceptionHandler
import forex.programs.rates.errors.ForexError

object ConnectionIOSyntax {

  implicit def toConnectionIOExec[A](cio: ConnectionIO[A]): ConnectionIOExecutor[A] =
    new ConnectionIOExecutor[A](cio)
}

final class ConnectionIOExecutor[T](private val connection: ConnectionIO[T]) extends AnyVal {

  def execute[F[_]]()(implicit transactor: Transactor[F],
                      bracket: Bracket[F, Throwable],
                      eh: ExceptionHandler): F[ForexError Either T] =
    Either
      .catchNonFatal {
        connection
          .transact(transactor)
          .attempt
          .map(_.leftMap(eh.handleException))
      }
      .valueOr(eh.handleException(_).asLeft.pure[F])
}
