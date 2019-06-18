package evolution.gaming.core

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.event.Logging
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import evolution.gaming.event.{IdWithInMessage, JoinedTable}
import evolution.gaming.factory.ObjectFactory
import evolution.gaming.workflow.WorkflowActor
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

class WorkflowActorSpec(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with BeforeAndAfterAll with MustMatchers with ImplicitSender {

  def this() = this(ActorSystem("AkkaTest"))

  override def afterAll {
    system.terminate()
  }

  "Workflow actor" must {
    "process Join, reply pong to ping message." in {

      lazy val log = Logging(system, classOf[WorkflowActorSpec])
      val authActor = TestProbe()
      val tableManagerActor = TestProbe()

      val workflowActor = system.actorOf(WorkflowActor.props(log, authActor.ref, tableManagerActor.ref), "workflowActor")

      val uuid = "sad23fdsf23r3456twsdfsa"
      val seq = 1

      workflowActor ! JoinedTable(uuid, testActor)
      workflowActor ! IdWithInMessage(uuid, ObjectFactory.ping(seq))

      expectMsg(1 second, ObjectFactory.pong(seq))
    }
  }
}