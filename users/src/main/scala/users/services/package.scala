package users

package object services {

  type UserManagement[F[_]] = usermanagement.Algebra[F]
  val UserManagement = usermanagement.Interpreters

}
