package forex.domain

sealed trait Tasks {
  protected var taskName: String = null
  protected def setName() : Unit
  def name() : String = this.taskName
}

object Tasks {
  case object FOREX_JOB extends Tasks {
    override def setName(): Unit = this.taskName = "FOREX_JOB"
  }
}

