package models

import models.contact._
import models.daos.StudioDao
import net.tobysullivan.google.geolocation.{GoogleGeolocationClient, GeoPoint}
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
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
      "website" -> studio.website.map(_.website),
      "geometry" -> Json.obj(
        "latitude" -> Try(Await.result(studio.getLatitude, 500 milliseconds)).toOption.flatten.map(_.toString),
        "longitude" -> Try(Await.result(studio.getLongitude, 500 milliseconds)).toOption.flatten.map(_.toString)
      )
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

  private lazy val geometry: Future[Option[GeoPoint]] = {
    address.map { address =>
      val addressBuilder = new StringBuilder()
      address.street.map(str => addressBuilder.append(str.street+", "))
      address.city.map(city => addressBuilder.append(city.city+", "))
      address.province.map(prov => addressBuilder.append(prov.province+", "))
      address.country.map(ctry => addressBuilder.append(ctry.country))

      GoogleGeolocationClient.getLatLong(addressBuilder.toString())
    }.getOrElse(Future(None))
  }

  def getLatitude: Future[Option[Float]] = geometry.map(_.map(_.lat))
  def getLongitude: Future[Option[Float]] = geometry.map(_.map(_.long))
}

