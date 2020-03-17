package users.api

import _root_.akka.actor.ActorSystem
import org.scalatra._
import users.domain._
import users.persistence.repositories.users.Repository
import users.services._

import scala.concurrent.{ExecutionContext, Future}
import spray.json._
import users.api.json.UserJsonProtocol._


class UsersScalatraServlet(system: ActorSystem) extends ScalatraServlet with FutureSupport {

  protected implicit def executor: ExecutionContext = system.dispatcher

  val interpreter = UserManagement.default(userRepository = Repository.inMemory())(executor)
  val pathPrefix = "/user"

  before() {
    contentType = "application/json"
  }

  /**
   * Returns all users as json.
   */
 get(s"${pathPrefix}s") {
   val allUsers: Future[Either[usermanagement.Error, List[User]]] = interpreter.all()

   allUsers.map {
     case Left(msg) => InternalServerError(s"Something went wrong \n\n $msg")
     case Right(users) => {
       val usersJson = users.map(_.toJson) // map every user to json
       Ok(usersJson)
     }
   }
  }

  /**
   * Returns signed up user.
   */
  put(s"$pathPrefix/signup") {
    val paramsJson = request.body
    val params = ApiUtils.jsonToMap(paramsJson)

    val signup: Future[Either[usermanagement.Error, User]] = interpreter.signUp(
      ApiUtils.getUserName(params),
      ApiUtils.getEmailAddress(params),
      ApiUtils.getOptionPassword(params))

    signup.map {
      case Left(msg) => InternalServerError(s"Something went wrong \n\n $msg")
      case Right(user) => {
        val userJson = user.toJson
        Ok(userJson)
      }
    }
  }

  /**
   * Returns user with a specific ID.
   */
  get(s"$pathPrefix/:id") {
    val userId = params("id")
    val user: Future[Either[usermanagement.Error, User]] = interpreter.get(User.Id(userId))

    user.map {
      case Left(msg) => InternalServerError(s"Something went wrong \n\n $msg")
      case Right(u) => {
        val userJson = u.toJson
        Ok(userJson)
      }
    }
  }

  /**
   * Returns blocked user.
   */
  get(s"$pathPrefix/block/:id") {
    val userId = params("id")
    val user: Future[Either[usermanagement.Error, User]] = interpreter.block(User.Id(userId))

    user.map {
      case Left(msg) => InternalServerError(s"Something went wrong \n\n $msg")
      case Right(u) => {
        val userJson = u.toJson
        Ok(userJson)
      }
    }
  }

  /**
   * Returns unblocked user.
   */
  get(s"$pathPrefix/unblock/:id") {
    val userId = params("id")
    val user: Future[Either[usermanagement.Error, User]] = interpreter.unblock(User.Id(userId))

    user.map {
      case Left(msg) => InternalServerError(s"Something went wrong \n\n $msg")
      case Right(u) => {
        val userJson = u.toJson
        Ok(userJson)
      }
    }
  }

  /**
   * Returns user with updated email.
   */
  post(s"$pathPrefix/update/email") {
    val paramsJson = request.body
    val params = ApiUtils.jsonToMap(paramsJson)

    val update: Future[Either[usermanagement.Error, User]] = interpreter.updateEmail(
      ApiUtils.getUserId(params),
      ApiUtils.getEmailAddress(params))

    update.map {
      case Left(msg) => InternalServerError(s"Something went wrong \n\n $msg")
      case Right(user) => {
        val userJson = user.toJson
        Ok(userJson)
      }
    }
  }

  /**
   * Returns user with updated password.
   */
  post(s"$pathPrefix/update/password") {
    val paramsJson = request.body
    val params = ApiUtils.jsonToMap(paramsJson)

    val update: Future[Either[usermanagement.Error, User]] = interpreter.updatePassword(
      ApiUtils.getUserId(params),
      ApiUtils.getPassword(params))

    update.map {
      case Left(msg) => InternalServerError(s"Something went wrong \n\n $msg")
      case Right(user) => {
        val userJson = user.toJson
        Ok(userJson)
      }
    }
  }

  /**
   * Returns user with reset password.
   */
  delete(s"$pathPrefix/password/:id") {
    val userId = params("id")

    val reset: Future[Either[usermanagement.Error, User]] = interpreter.resetPassword(User.Id(userId))

    reset.map {
      case Left(msg) => InternalServerError(s"Something went wrong \n\n $msg")
      case Right(user) => {
        val userJson = user.toJson
        Ok(userJson)
      }
    }
  }

  /**
   * Returns 200 if user is deleted.
   */
  delete(s"$pathPrefix/:id") {
    val userId = params("id")

    val done: Future[Either[usermanagement.Error, Done]] = interpreter.delete(User.Id(userId))
    done.map {
      case Left(msg) => InternalServerError(s"Something went wrong \n\n $msg")
      case Right(_) => Ok(userId)
    }
  }

}
