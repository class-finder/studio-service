package models

import java.util.UUID

import models.contact._
import play.api.libs.json.{Json, JsObject, Writes}

object Studio {
  implicit val studioWrites: Writes[Studio] = new Writes[Studio] {
    def writes(studio: Studio) = Json.obj(
      "studioId" -> studio.studioId.map(_.withDashes),
      "name" -> studio.name.map(_.name)
    )
  }
}

case class Studio(
                   studioId: Option[ObjectID],
                   name: Option[BusinessName],
                   address: Option[Address],
                   phone: Option[PhoneNumber],
                   website: Option[Website]
                   ) {
  override def toString: String = s"Studio(${studioId.map(_.withDashes).getOrElse("No ID")}, ${name.map(_.name).getOrElse("Unnamed")})"
}

