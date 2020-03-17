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
  val pathPrefix = "/users"

  before() {
    contentType = "application/json"
  }

  /**
   * Returns all users as json.
   */
 get(s"$pathPrefix") {
   val allUsers: Future[Either[usermanagement.Error, List[User]]] = interpreter.all()

   allUsers.map {
     case Left(msg) => InternalServerError(msg)
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
      ApiUtils.getPassword(params))

    signup.map {
      case Left(msg) => InternalServerError(msg)
      case Right(user) => {
        val userJson = user.toJson
        Ok(userJson)
      }
    }
  }


}
