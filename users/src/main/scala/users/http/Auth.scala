package users.http

import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.*
import org.typelevel.ci.CIString

import cats.*
import cats.data.*
import cats.implicits.*

import users.domain.User
import users.services.UserManagement

trait Auth[F[_]: Monad] extends Http4sDsl[F]:

  def userService: UserManagement[F]

  private val authUserEither: Kleisli[F, Request[F], Either[String, User]] =
    Kleisli { request =>
      val token = for {
        header <- EitherT(
                    request.headers
                      .get(CIString("Token"))
                      .map(_.head.value)
                      .toRight("Failed to authenticate a user")
                      .pure[F]
                  )
        id <- EitherT(
                Either
                  .catchNonFatal(User.Id(header))
                  .leftMap(_.toString)
                  .pure[F]
              )
        user <- EitherT(userService.get(id)).leftMap { case _ =>
                  "Can't find the user"
                }
      } yield user
      token.value
    }

  private val adminUser =
    authUserEither.andThen(Kleisli[F, Either[String, User], Either[String, User]] {
      case Left(err) => err.asLeft.pure
      case Right(u) if u.isAdmin => u.asRight.pure
      case Right(u) => "This user doesn't have access here".asLeft.pure
    })

  private val onFailure: AuthedRoutes[String, F] =
    Kleisli(req => OptionT.liftF(Forbidden(req.context)))

  val authMiddleware = AuthMiddleware(authUserEither, onFailure)

  val adminMiddleware = AuthMiddleware(adminUser, onFailure)
