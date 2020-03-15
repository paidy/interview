package api.json

import java.time.OffsetDateTime

import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.junit.JUnitRunner
import spray.json._
import users.api.json.UserJsonProtocol._
import users.domain.User.Metadata
import users.domain.{EmailAddress, Password, User, UserName}

@RunWith(classOf[JUnitRunner])
class UserJsonProtocolTest extends FlatSpec with Matchers {

  val jsonTest =
    """
      |{
      |  "id": "id_1",
      |  "userName": "John",
      |  "emailAddress": "john@test.com",
      |  "password": "nbtd56gtnte",
      |  "metadata": {
      |    "version": 1,
      |    "createdAt": "2020-03-15T15:51:15.967998+01:00",
      |    "updatedAt": "2020-03-15T15:51:15.969310+01:00",
      |    "blockedAt": "",
      |    "deletedAt": ""
      |  }
      |}
      |""".stripMargin

 it should "serialize User" in {

   val user = User(
     User.Id("id_1"),
     UserName("John"),
     EmailAddress("john@test.com"),
     Some(Password("nbtd56gtnte")),
     Metadata(1, OffsetDateTime.parse("2020-03-15T15:51:15.967998+01:00"), OffsetDateTime.parse("2020-03-15T15:51:15.967998+01:00"), None, None))

   val userJson = user.toJson

   print(userJson.prettyPrint)

   userJson == jsonTest

 }

  it should "deserialize User" in {

    // parse json
    val userJsValue: JsValue = jsonTest.parseJson
    // convert to case class
    val user = userJsValue.convertTo[User]

    // comparison
    jsonTest == user.toJson.prettyPrint

  }

}
