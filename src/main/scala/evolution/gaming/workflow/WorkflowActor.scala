package evolution.gaming.workflow

import akka.actor._
import akka.event.LoggingAdapter
import akka.pattern.ask
import akka.util.Timeout
import evolution.gaming.core._
import evolution.gaming.event._
import evolution.gaming.factory.ObjectFactory
import evolution.gaming.role.Roles._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object WorkflowActor {
  def props(log: LoggingAdapter, authActor: ActorRef, tableManagerActor: ActorRef)
           (implicit ec: ExecutionContext): Props = Props(new WorkflowActor(log, authActor, tableManagerActor))
}

class WorkflowActor(val log: LoggingAdapter,
                    val authActor: ActorRef,
                    val tableManagerActor: ActorRef)(implicit ec: ExecutionContext) extends Actor {
  var connected = Map.empty[String, (Option[String], ActorRef)]
  implicit val timeout: Timeout = Timeout(1 seconds)

  def actorRefById(connectId: String): ActorRef = connected(connectId)._2

  def usernameById(connectId: String): Option[String] = connected(connectId)._1

  def isAlreadyAuthorizedFromDifferentMachine(currentConnectId: String, currentUsername: String) = {
    val opt = connected
      .find {
        case (connectId, (username, _)) => username.contains(currentUsername) && connectId != currentConnectId
      }
    opt.isDefined
  }

  def replaceValueByKey(key: String, username: Option[String], ref: ActorRef): Unit = {
    connected -= key
    connected += (key -> (username, ref))
  }

  override def receive: Receive = {
    case IdWithInMessage(connectId, message) =>
      message match {

        case auth: ObjectFactory.login => {

          log.info(s"authentication: $connectId, ${auth.username}")

          (authActor ? auth) map {
            case Role(role) =>
              if (isAlreadyAuthorizedFromDifferentMachine(connectId, auth.username)) {
                actorRefById(connectId) ! ObjectFactory.fail("Already authorized from different machine")
              } else {
                val ref = actorRefById(connectId)

                role match {
                  case Some(Admin) =>
                    replaceValueByKey(connectId, Some(auth.username), ref)
                    ref ! ObjectFactory.login_successful(user_type = "admin")

                  case Some(User) =>
                    replaceValueByKey(connectId, Some(auth.username), ref)
                    ref ! ObjectFactory.login_successful(user_type = "user")

                  case None => ref ! ObjectFactory.login_failed
                }
              }
          }
        }

        case ObjectFactory.subscribe_tables =>
          usernameById(connectId) match {
            case None => actorRefById(connectId) ! ObjectFactory.not_authorized
            case Some(name) =>
              for (Role(role) <- authActor ? RoleByNameRequest(name))
                if (AuthActor.isAuthed(role)) {
                  for (TablesEvent(tables) <- tableManagerActor ? IdWithInMessage(name, message))
                    actorRefById(connectId) ! ObjectFactory.table_list(tables.toList)
                } else {
                  actorRefById(connectId) ! ObjectFactory.not_authorized
                }
          }

        case ObjectFactory.unsubscribe_tables =>
          usernameById(connectId) match {
            case None => actorRefById(connectId) ! ObjectFactory.not_authorized
            case Some(name) =>
              (authActor ? RoleByNameRequest(name))
                .map {
                  case Role(role) =>
                    if (AuthActor.isAuthed(role)) {
                      tableManagerActor ! IdWithInMessage(name, message)
                    } else {
                      actorRefById(connectId) ! ObjectFactory.not_authorized
                    }
                }
          }

        case ObjectFactory.add_table(_, _) |
             ObjectFactory.update_table(_) |
             ObjectFactory.remove_table(_) =>

          usernameById(connectId) match {
            case None => actorRefById(connectId) ! ObjectFactory.not_authorized
            case Some(name) =>
              (authActor ? RoleByNameRequest(name))
                .map {
                  case Role(role) =>
                    if (AuthActor.isAdmin(role)) {
                      (tableManagerActor ? message).map {
                        case TableEvent(event, subscribers) =>
                          subscribers.foreach { s =>
                            connected
                              .find {
                                case (_, (username, _)) => username.contains(s)
                              }
                              .foreach {
                                case (_, (_, ref)) => ref ! event
                              }
                          }
                      }
                    } else {
                      actorRefById(connectId) ! ObjectFactory.not_authorized
                    }
                }
          }

        case ObjectFactory.ping(seq) =>
          actorRefById(connectId) ! ObjectFactory.pong(seq)

        case msg: ObjectFactory.fail =>
          actorRefById(connectId) ! msg

        case unmatched =>
          log.error(s"Unmatched message: ${unmatched.toString}")
          actorRefById(connectId) ! ObjectFactory.fail(s"Unmatched message: ${unmatched.toString}")
      }

    case JoinedTable(connectId, ref) =>
      log.info(s"new user: $connectId")
      connected += (connectId -> (None, ref))

    case LeftTable(connectId) =>
      log.info(s"user left: $connectId")
      connected = connected.filterNot(_._1 == connectId)

    case unmatched =>
      log.error(s"Unmatched message: ${unmatched.toString}")
  }
}