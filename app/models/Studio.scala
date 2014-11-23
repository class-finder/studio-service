package models

import java.util.UUID

import models.contact._

case class Studio(
                   studioId: Option[ObjectID],
                   name: Option[BusinessName],
                   address: Option[Address],
                   phone: Option[PhoneNumber],
                   website: Option[Website]
                   ) {
  override def toString: String = s"Studio(${studioId.map(_.withDashes).getOrElse("No ID")}, ${name.map(_.name).getOrElse("Unnamed")})"
}

