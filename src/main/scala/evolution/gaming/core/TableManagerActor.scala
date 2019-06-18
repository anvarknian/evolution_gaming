package evolution.gaming.core

import akka.actor.{Actor, Props}
import akka.event.LoggingAdapter
import evolution.gaming.factory.ObjectFactory._
import evolution.gaming.core.TablesManager._
import evolution.gaming.event.{IdWithInMessage, TableEvent, TablesEvent}
import evolution.gaming.factory.ObjectFactory

import scala.language.postfixOps

object TableManagerActor {
  def props(log: LoggingAdapter): Props = Props(new TableManagerActor(log))
}

class TableManagerActor(val log: LoggingAdapter) extends Actor {
  var tables: Vector[Table] = Vector[Table]()
  var subscribers: Vector[String] = Vector[String]()

  def calcInsertId(afterId: Int): Int = {
    if (tables.isEmpty && afterId >= -1) afterId + 1
    else if (afterId < -1) -1
    else if (afterId == -1) {
      tables.map(_.id).min - 1
    } else {
      val larderIds = tables.map(_.id).filter(_ > afterId)
      Stream.from(afterId + 1) filter (id => !larderIds.contains(id)) head
    }
  }

  override def receive: Receive = {
    case IdWithInMessage(name, message) =>
      message match {
        case ObjectFactory.subscribe_tables =>
          subscribers = subscribers :+ name
          sender() ! TablesEvent(
            TablesManager.listOfPrivateTablesToPublic(tables.sortBy(_.id)))

        case ObjectFactory.unsubscribe_tables =>
          subscribers = subscribers.filterNot(_ == name)

        case unmatched =>
          log.error(s"Unmatched message: ${unmatched.toString}")
      }

    case add_table(after_id, table) =>
      val newId = calcInsertId(after_id)
      if (newId < 0) {
        sender() ! TableEvent(add_failed(newId), subscribers)
      } else {
        val add = TablesManager.publicToPrivate(table, newId)
        tables = tables :+ add
        sender() ! TableEvent(
          table_added(after_id, TablesManager.privateToPublic(add)),
          subscribers)
      }

    case update_table(table) =>
      val edit = TablesManager.publicToPrivate(table)
      if (!tables.map(_.id).contains(edit.id)) {
        sender() ! TableEvent(update_failed(edit.id), subscribers)
      } else {
        tables = tables.filterNot(_.id == edit.id)
        tables = tables :+ edit
        sender() ! TableEvent(table_updated(TablesManager.privateToPublic(edit)), subscribers)
      }

    case remove_table(id) =>
      if (!tables.map(_.id).contains(id)) {
        sender() ! TableEvent(removal_failed(id), subscribers)
      } else {
        tables = tables.filterNot(_.id == id)
        sender() ! TableEvent(table_removed(id), subscribers)
      }
  }

}