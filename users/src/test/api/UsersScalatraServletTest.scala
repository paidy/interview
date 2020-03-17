
import users.api.{ApiUtils, Keys, UsersScalatraServlet}
import org.scalatra.test.specs2._
import akka.actor.ActorSystem
import org.apache.http.client.methods.{HttpGet, HttpPost, HttpPut}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils

class UsersScalatraServletTest extends MutableScalatraSpec {


  val system = ActorSystem()
  val pathPrefix = "/user"

  addServlet(new UsersScalatraServlet(system), "/*")


  /**
   * Curl command:
   *
   * curl \
   * --header "Content-type: application/json" \
   * --request PUT \
   * --data '{"userName":"john","emailAddress":"john@test.com", "password":"be6eg1"}' \
   * http://localhost:8080/users/signup
   *
   */
  "PUT signup / on UsersScalatraServlet" should {
    "return User with input data" in {
      put(s"$pathPrefix/signup") {

        val dataActual = """{"userName":"john","emailAddress":"john@test.com", "password":"be6eg1"}"""

        val client = HttpClientBuilder.create.build
        val request = new HttpPut(s"http://localhost:8080/$pathPrefix/signup")
        request.setHeader("Content-type", "application/json")
        request.setEntity(new StringEntity(dataActual))

        val response = client.execute(request)

        response.getAllHeaders.foreach(arg => println(arg))
        val dataExpected = EntityUtils.toString(response.getEntity())
        println(dataExpected)

        val keysToCompare = List(Keys.userName, Keys.emailAddress, Keys.password)
        compareJsonKeys(keysToCompare, dataActual, dataExpected)
      }
    }
  }


  /**
   * Function compares all 'keys' of two json strings.
   *
   * @param keys            keys of json for comparison
   * @param dataActual      actual data returned by routes
   * @param dataExpected    expected data
   * @return                true/false
   */
  private def compareJsonKeys(keys: List[String], dataActual: String, dataExpected: String): Boolean = {
    val dataMapActual = ApiUtils.jsonToMap(dataActual)
    val dataMapExpected = ApiUtils.jsonToMap(dataExpected)

    keys.forall(k => dataMapExpected.get(k) == dataMapActual.get(k))
  }

}
