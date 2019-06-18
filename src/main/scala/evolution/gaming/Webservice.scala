package evolution.gaming

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.scaladsl.Flow
import evolution.gaming.factory.ObjectFactory
import evolution.gaming.workflow.Workflow
import evolution.gaming.json.JsonModule

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class Webservice(webSocket: String)(implicit system: ActorSystem, executionContext: ExecutionContext) extends Directives {

  val workflow: Workflow = Workflow.create(system)
  lazy val logger = Logging(system, classOf[Webservice])

  def route: Route = {
    pathPrefix(webSocket) {
      handleWebSocketMessages(websocketChatFlow)
    }
  }

  def websocketChatFlow: Flow[Message, Message, Any] = {
    Flow[Message]
      .collect {
        case TextMessage.Strict(message) ⇒ message
      }
      .map(JsonModule.decode)
      .map {
        case Right(message) => message
        case Left(err) =>
          logger.error(err.toString)
          ObjectFactory.fail(s"ERROR: ${err}")
      }
      .via(workflow.flow)
      .map {
        message: ObjectFactory.Message ⇒ TextMessage.Strict(JsonModule.toJson(message))
      }
      .via(reportErrorsFlow)
  }

  def reportErrorsFlow[T]: Flow[T, T, Any] =
    Flow[T].watchTermination()((_, f) => f.onComplete {
      case Success(_) =>
      case Failure(e) => logger.error(s"ERROR: ${e.getMessage}")
    })
}