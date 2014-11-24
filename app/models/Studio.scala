package models

import java.util.UUID

import models.contact._
import play.api.libs.json._

object Studio {
  implicit val studioReads: Reads[Studio] = new Reads[Studio] {
    def reads(json: JsValue): JsResult[Studio] = JsSuccess[Studio] {
      val studioId = (json \ "studioId").asOpt[String].map(ObjectID.apply)
      val name = (json \ "name").asOpt[String].map(BusinessName)

      Studio(studioId, name)
    }
  }

  implicit val studioWrites: Writes[Studio] = new Writes[Studio] {
    def writes(studio: Studio) = Json.obj(
      "studioId" -> studio.studioId.map(_.withDashes),
      "name" -> studio.name.map(_.name)
    )
  }
}

case class Studio(
                   studioId: Option[ObjectID] = None,
                   name: Option[BusinessName] = None,
                   address: Option[Address] = None,
                   phone: Option[PhoneNumber] = None,
                   website: Option[Website] = None
                   ) {
  override def toString: String = s"Studio(${studioId.map(_.withDashes).getOrElse("No ID")}, ${name.map(_.name).getOrElse("Unnamed")})"
}

