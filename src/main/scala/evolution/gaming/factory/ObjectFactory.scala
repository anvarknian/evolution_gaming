package evolution.gaming.factory


object ObjectFactory {

  sealed trait Message

  // User object
  case class login(username: String, password: String) extends Message

  // Table object
  case class table(id: Option[Int], name: String, participants: Int)

  // components
  case class table_list(tables: List[table]) extends Message

  // Login status objects
  case class login_successful(user_type: String) extends Message

  object login_failed extends Message

  // PingPong
  case class ping(seq: Int) extends Message

  case class pong(seq: Int) extends Message

  // Failure object
  case class fail(message: String) extends Message

  //User's right object
  case object not_authorized extends Message

  // User subscription/unsubscription objects
  object subscribe_tables extends Message

  object unsubscribe_tables extends Message


  // User's commands [ADMIN]
  case class add_table(after_id: Int, table: table) extends Message

  case class update_table(table: table) extends Message

  case class remove_table(id: Int) extends Message

  case class add_failed(id: Int) extends Message

  case class removal_failed(id: Int) extends Message

  case class update_failed(id: Int) extends Message

  // Events from the server objects
  case class table_added(after_id: Int, table: table) extends Message

  case class table_removed(id: Int) extends Message

  case class table_updated(table: table) extends Message

}