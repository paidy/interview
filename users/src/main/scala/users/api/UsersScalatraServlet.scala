package users.api

import _root_.akka.actor.ActorSystem
import org.scalatra._
import users.domain._
import users.persistence.repositories.users.Repository
import users.services._

import scala.concurrent.{ExecutionContext, Future}
import spray.json._
import users.api.json.UserJsonProtocol._

object UsersScalatraServlet {

  val pathPrefix = "/user"

  def getInternalServerErrorResponse(msg: users.services.usermanagement.Error): ActionResult = {
    InternalServerError(s"Something went wrong \n\n $msg")
  }

  def getRequestParams(body: String): Map[String, JsValue] = {
    ApiUtils.jsonToMap(body)
  }

}

class UsersScalatraServlet(system: ActorSystem) extends ScalatraServlet with FutureSupport {

  protected implicit def executor: ExecutionContext = system.dispatcher
  val interpreter = UserManagement.default(userRepository = Repository.inMemory())(executor)

  before() {
    contentType = "application/json"
  }

  /**
   * Returns all users as json.
   */
 get(s"${UsersScalatraServlet.pathPrefix}s") {
   val allUsers: Future[Either[usermanagement.Error, List[User]]] = interpreter.all()

   allUsers.map {
     case Left(msg) => UsersScalatraServlet.getInternalServerErrorResponse(msg)
     case Right(users) => {
       val usersJson = users.map(_.toJson) // map every user to json
       Ok(usersJson)
     }
   }
  }

  /**
   * Returns signed up user.
   */
  put(s"${UsersScalatraServlet.pathPrefix}/signup") {
    val params = UsersScalatraServlet.getRequestParams(request.body)

    val signup: Future[Either[usermanagement.Error, User]] = interpreter.signUp(
      ApiUtils.getUserName(params),
      ApiUtils.getEmailAddress(params),
      ApiUtils.getOptionPassword(params))

    signup.map {
      case Left(msg) => UsersScalatraServlet.getInternalServerErrorResponse(msg)
      case Right(user) => {
        val userJson = user.toJson
        Ok(userJson)
      }
    }
  }

  /**
   * Returns user with a specific ID.
   */
  get(s"${UsersScalatraServlet.pathPrefix}/:id") {
    val userId = params("id")
    val user: Future[Either[usermanagement.Error, User]] = interpreter.get(User.Id(userId))

    user.map {
      case Left(msg) => UsersScalatraServlet.getInternalServerErrorResponse(msg)
      case Right(u) => {
        val userJson = u.toJson
        Ok(userJson)
      }
    }
  }

  /**
   * Returns blocked user.
   */
  post(s"${UsersScalatraServlet.pathPrefix}/block") {
    val params = UsersScalatraServlet.getRequestParams(request.body)
    val user: Future[Either[usermanagement.Error, User]] = interpreter.block(ApiUtils.getUserId(params))

    user.map {
      case Left(msg) => UsersScalatraServlet.getInternalServerErrorResponse(msg)
      case Right(u) => {
        val userJson = u.toJson
        Ok(userJson)
      }
    }
  }

  /**
   * Returns unblocked user.
   */
  post(s"${UsersScalatraServlet.pathPrefix}/unblock") {
    val params = UsersScalatraServlet.getRequestParams(request.body)
    val user: Future[Either[usermanagement.Error, User]] = interpreter.unblock(ApiUtils.getUserId(params))

    user.map {
      case Left(msg) => UsersScalatraServlet.getInternalServerErrorResponse(msg)
      case Right(u) => {
        val userJson = u.toJson
        Ok(userJson)
      }
    }
  }

  /**
   * Returns user with updated email.
   */
  post(s"${UsersScalatraServlet.pathPrefix}/update/email") {
    val params = UsersScalatraServlet.getRequestParams(request.body)

    val update: Future[Either[usermanagement.Error, User]] = interpreter.updateEmail(
      ApiUtils.getUserId(params),
      ApiUtils.getEmailAddress(params))

    update.map {
      case Left(msg) => UsersScalatraServlet.getInternalServerErrorResponse(msg)
      case Right(user) => {
        val userJson = user.toJson
        Ok(userJson)
      }
    }
  }

  /**
   * Returns user with updated password.
   */
  post(s"${UsersScalatraServlet.pathPrefix}/update/password") {
    val params = UsersScalatraServlet.getRequestParams(request.body)

    val update: Future[Either[usermanagement.Error, User]] = interpreter.updatePassword(
      ApiUtils.getUserId(params),
      ApiUtils.getPassword(params))

    update.map {
      case Left(msg) => UsersScalatraServlet.getInternalServerErrorResponse(msg)
      case Right(user) => {
        val userJson = user.toJson
        Ok(userJson)
      }
    }
  }

  /**
   * Returns user with reset password.
   */
  delete(s"${UsersScalatraServlet.pathPrefix}/password/delete") {
    val params = UsersScalatraServlet.getRequestParams(request.body)

    val reset: Future[Either[usermanagement.Error, User]] = interpreter.resetPassword(ApiUtils.getUserId(params))

    reset.map {
      case Left(msg) => UsersScalatraServlet.getInternalServerErrorResponse(msg)
      case Right(user) => {
        val userJson = user.toJson
        Ok(userJson)
      }
    }
  }

  /**
   * Returns 200 if user is deleted.
   */
  delete(s"${UsersScalatraServlet.pathPrefix}/delete") {
    val params = UsersScalatraServlet.getRequestParams(request.body)
    val userId = ApiUtils.getUserId(params)

    val done: Future[Either[usermanagement.Error, Done]] = interpreter.delete(userId)
    done.map {
      case Left(msg) => UsersScalatraServlet.getInternalServerErrorResponse(msg)
      case Right(_) => Ok(userId.value)
    }
  }

}
