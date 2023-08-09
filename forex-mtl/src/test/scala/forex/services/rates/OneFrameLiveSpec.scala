package forex.services.rates

import cats.effect.IO
import cats.implicits._
import cats.effect.testing.scalatest.AsyncIOSpec
import forex.domain.{ Currency, Price, Rate, Timestamp }
import forex.services.StorageService
import forex.services.storage
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

class OneFrameLiveSpec extends AsyncWordSpecLike with AsyncIOSpec with Matchers with EitherValues {

  "OneFrameLive" should {
    "return an error on missing pair" in {
      val repo = new StorageService[IO] {
        override def get(pair: Rate.Pair): IO[Either[storage.errors.Error, Rate]] =
          IO.pure(storage.errors.Error.PairLookupFailed(s"Failed to get rate for the pair ${pair.show}").asLeft)
        override def put(rate: Rate): IO[Either[storage.errors.Error, Unit]] = ???
      }

      val service = Interpreters.live(repo)

      service.get(Rate.Pair(Currency.GBP, Currency.USD)).map { result =>
        result shouldBe Symbol("Left")

        result.left.value shouldBe a[errors.Error.OneFrameLookupFailed]
      }
    }

    "return a value for a pair" in {
      val repo = new StorageService[IO] {
        override def get(pair: Rate.Pair): IO[Either[storage.errors.Error, Rate]] =
          IO.pure(Rate(pair = pair, price = Price(10), timestamp = Timestamp.now).asRight)

        override def put(rate: Rate): IO[Either[storage.errors.Error, Unit]] = ???
      }

      val service = Interpreters.live(repo)

      service.get(Rate.Pair(Currency.GBP, Currency.USD)).map { result =>
        result shouldBe Symbol("Right")

        result.value.price shouldBe Price(10)
      }
    }
  }

}
