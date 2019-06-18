package evolution.gaming.core

import evolution.gaming.factory.ObjectFactory.table

import scala.collection.immutable.Seq


object TablesManager {

  case class Table(id: Int, title: String, participants: Int)

  def privateToPublic(privateTable: Table) = privateTable match {
    case Table(id, title, participants) => table(Some(id), title, participants)
  }

  def publicToPrivate(publicTable: table, newId: Int = -1) = publicTable match {
    case table(Some(id), title, participants) => Table(id, title, participants)
    case table(None, title, participants) => Table(newId, title, participants)
  }

  def listOfPrivateTablesToPublic(tables: Seq[Table]) = tables.map(privateToPublic)
}