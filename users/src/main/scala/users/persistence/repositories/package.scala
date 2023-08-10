package users.persistence

package object repositories {

  // User Repository
  type UserRepository[F[_]] = users.Repository[F]
  val UserRepository = users.Repository

}
