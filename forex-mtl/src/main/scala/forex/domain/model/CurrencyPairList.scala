package forex.domain.model

object CurrencyPairList {
  val all: Seq[Rate.Pair] =
    Currency.all
      .flatMap { from =>
        Currency.all.map { to =>
          Rate.Pair(from, to)
        }
      }
      .filterNot(pair => pair.from == pair.to)
}
