package forex.components.scheduler.tasks

import forex.domain.Tasks

import scala.collection.mutable

object TasksLibrary {
  private val tasksSpawned: mutable.Map[String, Boolean] = mutable.Map[String, Boolean]()

  def isTaskSpawned(task: Tasks) : Boolean = tasksSpawned(task.name())

  private[tasks] def addSpawnedTask(task: Tasks) : Option[Boolean] = tasksSpawned.put(task.name(), true)
}
