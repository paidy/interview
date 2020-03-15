package users.api.json

import java.time.OffsetDateTime

import spray.json.{DefaultJsonProtocol, JsNumber, JsObject, JsString, JsValue, RootJsonFormat, _}
import users.domain.User.{Id, Metadata}
import users.domain.{EmailAddress, Password, User, UserName}

import scala.collection.immutable.ListMap

object UserJsonProtocol extends DefaultJsonProtocol {

  implicit object MetadataFormat extends RootJsonFormat[Metadata] {

    def write(metadata: Metadata) = JsObject(ListMap(
      "version" -> JsNumber(metadata.version),
      "createdAt" -> JsString(metadata.createdAt.toString),
      "updatedAt" -> JsString(metadata.updatedAt.toString),
      "blockedAt" -> (if (metadata.blockedAt.isDefined) JsString(metadata.blockedAt.toString) else JsString("")),
      "deletedAt" -> (if (metadata.deletedAt.isDefined) JsString(metadata.deletedAt.toString) else JsString(""))
    ))

    def read(json: JsValue): Metadata = {
      val fields = json.asJsObject.fields
      val blockedAt = fields("blockedAt").convertTo[String]
      val deletedAt = fields("deletedAt").convertTo[String]

      Metadata(
        fields("version").convertTo[Int],
        OffsetDateTime.parse(fields("createdAt").convertTo[String]),
        OffsetDateTime.parse(fields("updatedAt").convertTo[String]),
        if (blockedAt.isEmpty) None else Some(OffsetDateTime.parse(blockedAt)),
        if (deletedAt.isEmpty) None else Some(OffsetDateTime.parse(deletedAt))
      )
    }
  }

  implicit object UserFormat extends RootJsonFormat[User] {

    def write(user: User) = JsObject(ListMap(
      "id" -> JsString(user.id.value),
      "userName" -> JsString(user.userName.value),
      "emailAddress" -> JsString(user.emailAddress.value),
      "password" -> (if (user.password.isDefined) JsString(user.password.get.value) else JsString("")),
      "metadata" -> user.metadata.toJson
    ))

    def read(json: JsValue): User = {
      val fields: Map[String, JsValue] = json.asJsObject.fields
      val password = fields("password").convertTo[String]

      User(
        Id(fields("id").convertTo[String]),
        UserName(fields("userName").convertTo[String]),
        EmailAddress(fields("emailAddress").convertTo[String]),
        if (password.isEmpty) None else Some(Password(password)),
        fields("metadata").convertTo[Metadata]
      )
    }

  }

}
