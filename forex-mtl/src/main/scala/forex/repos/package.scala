package forex

package object repos {
  type RatesRepo[F[_]] = rates.Algebra[F]

  final val RatesRepos = rates.Interpreters
}
