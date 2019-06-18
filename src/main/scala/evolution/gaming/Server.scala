package evolution.gaming

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object ServerApp {

  var actorSystem: Option[ActorSystem] = None
  var serverBinding: Option[Future[ServerBinding]] = None

  def start: String = {
    implicit val system: ActorSystem = ActorSystem("httpActor")
    actorSystem = Some(system)
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val config = system.settings.config
    val host = config.getString("app.host")
    val port = config.getInt("app.port")
    val webSocket = config.getString("app.webSocket")

    val service = new Webservice(webSocket)
    lazy val routes: Route = Route.seal {
      service.route
    }

    val binding: Future[ServerBinding] = Http().bindAndHandle(routes, host, port)
    serverBinding = Some(binding)
    binding.onComplete {
      case Success(binding) ⇒
        val localAddress = binding.localAddress
        println(s"Server is listening on ${localAddress.getHostName}:${localAddress.getPort}")
      case Failure(e) ⇒
        println(s"Binding failed with ${e.getMessage}")
        system.terminate()
    }
    s"ws://$host:$port/$webSocket"
  }

  def stop: Unit = (actorSystem, serverBinding) match {
    case (Some(system), Some(binding)) =>
      binding
        .flatMap(_.unbind())
        .onComplete(_ => {
          system.terminate()
          println("Server stopped")
          actorSystem = None
          serverBinding = None
        })
    case _ => ()
  }


}

object Server extends App {
  ServerApp.start
}