package users.persistence.repositories.users

import cats.Applicative

import users.domain.*

private[repositories] trait Repository[F[_]]:
  def insert(user: User): F[Done]
  def get(id: User.Id): F[Option[User]]
  def getByUserName(userName: UserName): F[Option[User]]
  def all(): F[List[User]]

object Repository:
  def inMemory[F[_]: Applicative](): Repository[F] = new InMemoryRepository[F]()
