package forex

package object services {
  type RatesIngestor[F[_]] = rates_ingestor.Algebra[F]
  type RatesService[F[_]]  = rates.Algebra[F]

  final val RatesIngestors = rates_ingestor.Interpreters
  final val RatesServices  = rates.Interpreters
}
