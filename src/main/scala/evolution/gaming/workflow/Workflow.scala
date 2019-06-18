package evolution.gaming.workflow

import akka.actor._
import akka.event.Logging
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import evolution.gaming.core._
import evolution.gaming.event.{Event, IdWithInMessage, JoinedTable}
import evolution.gaming.factory.ObjectFactory

import scala.concurrent.ExecutionContext

trait Workflow {
  def flow: Flow[ObjectFactory.Message, ObjectFactory.Message, Any]
}

object Workflow {
  def uuid = java.util.UUID.randomUUID.toString

  def create(system: ActorSystem)(implicit ec: ExecutionContext): Workflow = {

    lazy val log = Logging(system, classOf[Workflow])
    val authActor = system.actorOf(AuthActor.props(log), "authenticationActor")
    val tableManagerActor = system.actorOf(TableManagerActor.props(log), "tableManagerActor")

    val workflowActor = system.actorOf(WorkflowActor.props(log, authActor, tableManagerActor), "workflowActor")

    new Workflow {
      def flow: Flow[ObjectFactory.Message, ObjectFactory.Message, Any] = {
        val connectId = uuid

        val input =
          Flow[ObjectFactory.Message]
            .collect { case message: ObjectFactory.Message => IdWithInMessage(connectId, message) }
            .to(Sink.actorRef[Event](workflowActor, Left(connectId)))

        val output =
          Source.actorRef[ObjectFactory.Message](10, OverflowStrategy.fail)
            .mapMaterializedValue(workflowActor ! JoinedTable(connectId, _))

        Flow.fromSinkAndSource(input, output)
      }
    }
  }
}