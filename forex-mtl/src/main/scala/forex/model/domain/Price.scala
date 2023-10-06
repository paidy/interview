package forex.model.domain


final case class Price(value: BigDecimal) extends AnyVal


object Price {
  def apply(value: Integer): Price =
    Price(BigDecimal(value))
}
