package forex.services.cache

import cats._
import cats.data.OptionT
import cats.implicits._
import io.circe.syntax._
import io.circe.parser.decode
import org.typelevel.log4cats.Logger
import dev.profunktor.redis4cats.RedisCommands
import forex.config.RedisConfig
import forex.domain.Rate


object Interpreter {
  def make[F[_]: Monad: Logger](
    redis: RedisCommands[F, String, String],
    config: RedisConfig
  ): Algebra[F] = {

    import Protocol._

    new Algebra[F] {
      override def get(pair: Rate.Pair): F[Option[Rate]] = {
        for {
          cacheStr <- redis.get(pair.show)
          rate <- cacheStr.fold(
            Logger[F].debug(s"Not found key: ${pair.show}").as(Option.empty[Rate])
          )(
            str => OptionT.fromOption[F](decode[Rate](str).toOption).value
          )
        } yield rate
      }

      override def set(pair: Rate.Pair, rate: Rate): F[Boolean] = {
        for {
          _ <- Logger[F].debug(s"Set ${pair.show}")
          _ <- redis.set(pair.show, rate.asJson.noSpaces)
          bool <- redis.expire(pair.show, config.expiredTime)
        } yield bool
      }
    }
  }
}
