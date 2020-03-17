
import _root_.akka.actor.ActorSystem
import javax.servlet.ServletContext
import org.scalatra._
import users.api.UsersScalatraServlet

class ScalatraBootstrap extends LifeCycle {

  val system = ActorSystem()

  override def init(context: ServletContext) {
    context.mount(new UsersScalatraServlet(system), "/*")
  }

  override def destroy(context:ServletContext) {
    system.terminate()
  }

}