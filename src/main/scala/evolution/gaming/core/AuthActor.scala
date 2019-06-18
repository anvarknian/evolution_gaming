package evolution.gaming.core

import akka.actor.{Actor, Props}
import akka.event.LoggingAdapter
import evolution.gaming.event.{Role, RoleByNameRequest}
import evolution.gaming.factory.ObjectFactory.login
import evolution.gaming.role.Roles._

object AuthActor {

  def props(log: LoggingAdapter): Props = Props(new AuthActor(log))

  def isAdmin(role: Option[RoleTrait]): Boolean = {
    role match {
      case Some(Admin) => true
      case _ => false
    }
  }

  def isAuthed(role: Option[RoleTrait]): Boolean = {
    role match {
      case Some(Admin) | Some(User) => true
      case _ => false
    }
  }
}

// auth actor handles all things related to authentication & authorization
class AuthActor(val log: LoggingAdapter) extends Actor {

  val dataBaseLikeLoginPassword = Map("admin" -> "admin", "user1234" -> "password1234")
  val dataBaseLikeRole = Map("admin" -> Admin, "user1234" -> User)

  override def receive: Receive = {
    case login(username, password) =>
      val r =
        dataBaseLikeLoginPassword
          .find(_ == (username, password))
          .flatMap { case (name, _) => dataBaseLikeRole.get(name) }
      sender() ! Role(r)

    case RoleByNameRequest(username) =>
      val received = dataBaseLikeRole.get(username)
      sender() ! Role(received)
  }
}