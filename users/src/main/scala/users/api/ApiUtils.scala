package users.api

import spray.json._
import DefaultJsonProtocol._
import users.domain.{EmailAddress, Password, UserName}

object Keys {
  val userName      = "userName"
  val emailAddress  = "emailAddress"
  val password      = "password"
}

object ApiUtils {

  def jsonToMap(json: String): Map[String, JsValue] = {
    json.parseJson.convertTo[Map[String, JsValue]]
  }

  def getUserName(params: Map[String, JsValue]): UserName = {
    val value = params.get(Keys.userName)
    value match {
      case Some(v) => UserName(v.convertTo[String])
      case None => throw new Exception("User name must be filled in.")
    }
  }

  def getEmailAddress(params: Map[String, JsValue]): EmailAddress = {
    val value = params.get(Keys.emailAddress)
    value match {
      case Some(v) => EmailAddress(v.convertTo[String])
      case None => throw new Exception("Email address must be filled in.")
    }
  }

  def getPassword(params: Map[String, JsValue]): Option[Password] = {
    val value: Option[JsValue] = params.get(Keys.password)
    value match {
      case Some(v) => Some(Password(v.convertTo[String]))
      case None => None
    }
  }

}
