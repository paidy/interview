package forex.services.storage

object errors {

  sealed trait Error {
    def msg: String
  }
  object Error {
    final case class PairLookupFailed(msg: String) extends Error
  }

}
