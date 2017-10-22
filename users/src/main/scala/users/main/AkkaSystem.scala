package users.main

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.data.Reader
import users.config.{AkkaConfig, HttpApplicationConfig}

object AkkaSystem {

  val reader: Reader[AkkaConfig, AkkaSystem] =
    Reader(AkkaSystem(_))

  val fromApplicationConfig: Reader[HttpApplicationConfig, AkkaSystem] =
    reader.local[HttpApplicationConfig](_.akka)

}

final case class AkkaSystem(
  akkaConfig: AkkaConfig
) {

  implicit val actorSystem: ActorSystem = ActorSystem(akkaConfig.name)

  implicit val materializer: ActorMaterializer = ActorMaterializer()

}
