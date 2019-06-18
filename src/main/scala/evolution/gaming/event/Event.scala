package evolution.gaming.event

import akka.actor.ActorRef
import evolution.gaming.factory.ObjectFactory._
import evolution.gaming.role.Roles.RoleTrait

import scala.collection.immutable.Seq

sealed trait Event

case class JoinedTable(connectId: String, ref: ActorRef) extends Event

case class LeftTable(connectId: String) extends Event

case class Role(role: Option[RoleTrait]) extends Event

case class IdWithInMessage(id: String, message: Message) extends Event

case class RoleByNameRequest(username: String) extends Event

case class TableEvent(event: Message, users: Seq[String])

case class TablesEvent(tables: Seq[table])