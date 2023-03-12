package com.example.simplerestaurantapi

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  val run = SimplerestaurantapiServer.run[IO]
}
