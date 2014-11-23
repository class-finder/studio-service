package daos

import java.util.UUID

import daos.paging.PagedResult
import models.{BusinessName, ObjectID, Studio}
import anorm._
import play.api.db.DB
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import scala.concurrent.Future

trait StudioDao {
  def createStudio(studio: Studio): Future[Try[Studio]] = ???

  def readStudio(studioId: ObjectID): Future[Try[Option[Studio]]] = Future {
    Try {
      DB.withConnection { implicit c =>
        val studioRow = SQL(
          """
            |SELECT HEX(`studio_id`) AS x_studio_id, `name`
            |FROM studio
            |WHERE `studio_id` = UNHEX({studioId})
            |LIMIT 1;
          """.stripMargin
        ).on("studioId" -> studioId.stripDashes).apply().headOption

        studioRow.map { row =>
          Studio(
            Some(ObjectID(row[String]("x_studio_id"))),
            Some(BusinessName(row[String]("name"))),
          None,
          None,
          None
          )
        }
      }
    }
  }

  def updateStudio(studio: Studio): Future[Try[Studio]] = ???

  def deleteStudio(studioId: ObjectID): Future[Try[Unit]] = ???

  def indexStudios(): Future[Try[PagedResult[Studio]]] = ???
}
