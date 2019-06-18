package evolution.gaming.role

object Roles {

  sealed trait RoleTrait

  object Admin extends RoleTrait

  object User extends RoleTrait

}