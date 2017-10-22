package users

import users.config._
import users.main.Application

trait WithApplication {

  lazy val config = ApplicationConfig(
    executors = ExecutorsConfig(
      services = ExecutorsConfig.ServicesConfig(
        parallellism = 1
      )
    ),
    services = ServicesConfig(
      users = ServicesConfig.UsersConfig(
        failureProbability = 0.0,
        timeoutProbability = 0.0
      )
    )
  )

  lazy val application: Application = Application.fromApplicationConfig.run(config)

}
