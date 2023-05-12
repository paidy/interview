package forex.module

import cats.effect.{ Blocker, ConcurrentEffect, ContextShift, Resource }
import com.zaxxer.hikari.HikariConfig
import doobie.{ ExecutionContexts, Transactor }
import doobie.hikari.HikariTransactor
import forex.config.ApplicationConfig
import forex.config.ApplicationConfig.DatabaseConfig
import forex.module.ResourcesModule.ResourcesDeps
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import sttp.client3.logging.slf4j.Slf4jLoggingBackend

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

class ResourcesModule[F[_]: ConcurrentEffect: ContextShift](config: ApplicationConfig) {

  private def make(config: ApplicationConfig): Resource[F, ResourcesDeps[F]] = {
    val transactPool = Executors.newCachedThreadPool()
    val blockerPool  = Blocker.liftExecutionContext(ExecutionContext.fromExecutor(transactPool))
    for {
      transactor <- transactor(config.database, blockerPool)
      backend <- makeStreamBackend(blockerPool)
    } yield ResourcesDeps(transactor, backend)
  }

  private def transactor(config: DatabaseConfig, blocker: Blocker): Resource[F, Transactor[F]] =
    for {
      connectionPool <- ExecutionContexts.fixedThreadPool(config.maximumPoolSize)
      hikariTransactor <- HikariTransactor.fromHikariConfig(hikariConfig(config), connectionPool, blocker)
    } yield hikariTransactor

  private def hikariConfig(config: DatabaseConfig): HikariConfig = {
    import config._

    val conf = new HikariConfig()
    conf.setJdbcUrl(url)
    conf.setUsername(user)
    conf.setPassword(password)
    conf.setConnectionTimeout(connectionTimeout.toMillis)
    conf.setValidationTimeout(validationTimeout.toMillis)
    conf.setMaximumPoolSize(maximumPoolSize)
    conf.setDriverClassName(driver)
    conf.setConnectionInitSql("SET TIME ZONE 'UTC'")
    conf.setConnectionTestQuery("select 1")
    conf
  }

  private def makeStreamBackend(blocker: Blocker): Resource[F, SttpBackend[F, Fs2Streams[F]]] =
    AsyncHttpClientFs2Backend.resource(blocker).map { client =>
      Slf4jLoggingBackend(client, logRequestBody = true, logResponseBody = true)
    }

  val resources: Resource[F, ResourcesDeps[F]] = make(config)

}

object ResourcesModule {

  def apply[F[_]: ConcurrentEffect: ContextShift](config: ApplicationConfig) = new ResourcesModule[F](config)

  case class ResourcesDeps[F[_]](transactor: Transactor[F], streamBackend: SttpBackend[F, Fs2Streams[F]])

}
