package models

import java.util.UUID

import play.api.mvc.PathBindable

import scala.util.Try

object ObjectID {
  implicit def pathBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[ObjectID] {
    override def bind(key: String, value: String): Either[String, ObjectID] = Right(ObjectID(value))

    override def unbind(key: String, value: ObjectID): String = value.id
  }

  def randomID: ObjectID = {
    val seed = UUID.randomUUID()
    ObjectID(seed.toString)
  }
}

case class ObjectID(id: String) extends AnyVal {
  private def withDashes: String = {
    if(id.length == 32) {
      val sb = new StringBuilder(36)
      sb.append(id.substring(0, 8))
      sb.append('-')
      sb.append(id.substring(8, 12))
      sb.append('-')
      sb.append(id.substring(12, 16))
      sb.append('-')
      sb.append(id.substring(16, 20))
      sb.append('-')
      sb.append(id.substring(20))
      sb.toString()
    } else if (id.length == 36) {
      id
    } else {
      throw new Exception("ID is not a valid GUID")
    }
  }

  def stripDashes: String = {
    if(id.length == 32) {
      id
    } else if (id.length == 36) {
      id.split('-').mkString
    } else {
      throw new Exception("ID is not a valid GUID")
    }
  }

  def tryGetUUID: Try[UUID] = Try { UUID.fromString(withDashes) }
}
