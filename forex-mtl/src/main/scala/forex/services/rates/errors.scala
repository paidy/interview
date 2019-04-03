package forex.services.rates

object errors {

  sealed trait Error
  object Error {
    final case class OneForgeLookupFailed(msg: String) extends Error
  }

}
