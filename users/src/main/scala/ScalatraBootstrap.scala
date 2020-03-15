
import org.scalatra._
import javax.servlet.ServletContext
import users.api.UsersScalatraServlet

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new UsersScalatraServlet, "/*")
  }
}