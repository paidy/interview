package users.persistence.repositories.users

import users.domain._

import scala.concurrent.Future

private[repositories] trait Repository {
  def insert(user: User): Future[Done]
  def get(id: User.Id): Future[Option[User]]
  def getByUserName(userName: UserName): Future[Option[User]]
  def all(): Future[List[User]]
}

object Repository {
  def inMemory(): Repository =
    new InMemoryRepository()
}
