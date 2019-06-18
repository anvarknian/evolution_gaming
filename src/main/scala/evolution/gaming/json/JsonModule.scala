package evolution.gaming.json

import evolution.gaming.factory.ObjectFactory._
import io.circe.Printer
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.auto._
import io.circe.syntax._

object JsonModule {

  implicit val genDevConfig: Configuration = Configuration.default.withDiscriminator("$type")

  def decode(message: String) = io.circe.parser.decode[Message](message)

  def toJson(message: Message) = message.asJson.pretty(Printer.noSpaces.copy(dropNullValues = true))
}
