package users

import cats.data._
import cats.implicits._

import users.config._
import users.main._
import scala.concurrent.duration._

object Main extends App {

  val config = HttpApplicationConfig(
    application = ApplicationConfig(
      executors = ExecutorsConfig(
        services = ExecutorsConfig.ServicesConfig(
          parallellism = 4
        )
      ),
      services = ServicesConfig(
        users = ServicesConfig.UsersConfig(
          failureProbability = 0.1,
          timeoutProbability = 0.1
        )
      )
    ),
    http = HttpConfig(
      port = 9000,
      host = "localhost",
      requestTimeout = 3 seconds
    ),
    akka = AkkaConfig("user-management-system")
  )

  val application = HttpApplication.fromApplicationConfig.run(config)

}
