package models

import models.contact._
import models.daos.StudioDao
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

object Studio {
  private val studioDao = new StudioDao {}

  implicit val studioReads: Reads[Studio] = new Reads[Studio] {
    def reads(json: JsValue): JsResult[Studio] = JsSuccess[Studio] {
      val studioId = (json \ "studioId").asOpt[String].map(ObjectID.apply)
      val name = (json \ "name").asOpt[String].map(BusinessName)

      Studio(studioId, name)
    }
  }

  implicit val studioWrites: Writes[Studio] = new Writes[Studio] {
    def writes(studio: Studio) = Json.obj(
      "studioId" -> studio.studioId.map(_.stripDashes),
      "name" -> studio.name.map(_.name),
      "address" -> studio.address,
      "phone" -> studio.phone.map(_.number),
      "website" -> studio.website.map(_.website)
    )
  }

  def load(studioId: ObjectID): Future[Try[Option[Studio]]] = studioDao.readStudio(studioId)

  def delete(studioId: ObjectID): Future[Try[Boolean]] = studioDao.deleteStudio(studioId)

  def all: Future[Try[Seq[Studio]]] = studioDao.indexStudios().map { t => t.map { paged => paged.result }}
}

case class Studio(
                   studioId: Option[ObjectID] = None,
                   name: Option[BusinessName] = None,
                   address: Option[Address] = None,
                   phone: Option[PhoneNumber] = None,
                   website: Option[Website] = None
                   ) {
  def save: Future[Try[Option[Studio]]] = studioId match {
    case None => Studio.studioDao.createStudio(this).map(t => t.map(Some(_)))
    case Some(id) => Studio.studioDao.updateStudio(id, this)
  }

  def delete: Future[Try[Boolean]] = studioId match {
    case None => Future(Try(throw new Exception("Studio ID must be set")))
    case Some(id) => Studio.delete(id)
  }
}

