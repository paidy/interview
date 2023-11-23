package forex

package object services {
  type RatesService[F[_]] = rates.Algebra[F]
}
