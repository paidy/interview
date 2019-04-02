package forex

import forex.domain._

package object programs {
  type RatesProgram[F[_]] = rates.Algebra[F]
  final val RatesProgram = rates.Program


  final case class GetRatesRequest(
      from: Currency,
      to: Currency
  )
}
