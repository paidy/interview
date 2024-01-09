package users.persistence.repositories.users

import scala.collection.concurrent.TrieMap

import cats.implicits.*
import cats.Applicative

import users.domain.*
import users.persistence.repositories.*

private[users] object InMemoryRepository:
  private final val UserMap: TrieMap[User.Id, User] = TrieMap.empty

private[users] class InMemoryRepository[F[_]: Applicative] extends UserRepository[F]:
  import InMemoryRepository.*

  def insert(user: User): F[Done] = {
    UserMap.update(user.id, user)
    Done
  }.pure[F]

  def get(id: User.Id): F[Option[User]] = UserMap.get(id).pure[F]

  def getByUserName(userName: UserName): F[Option[User]] =
    UserMap
      .collectFirst {
        case (_, user) if user.userName === userName => user
      }
      .pure[F]

  def all(): F[List[User]] = UserMap.values.toList.pure[F]
