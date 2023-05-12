package forex.clients.rates

import forex.clients.rates.protocol.OneFrameRate
import forex.programs.rates.errors.ForexError

trait OneFrameClientAlgebra[F[_]] {

  def getRates: F[ForexError Either List[OneFrameRate]]

}
