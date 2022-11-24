package client

import o1.*
import o1.gui.Color

object HandleData {

  def handle(data: Option[Data]) =
    data match
      case None => println("Error")
      case Some(obj) =>
	println(obj)
}
