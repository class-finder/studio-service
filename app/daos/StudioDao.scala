package daos

import java.util.UUID

import daos.paging.{TotalResults, PageNumber, ResultsPerPage, PagedResult}
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
            |LIMIT 1
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

  def indexStudios(): Future[Try[PagedResult[Studio]]] = Future {
    Try {
      DB.withConnection { implicit c =>
        val studioRows = SQL(
          """
            |SELECT SQL_CALC_FOUND_ROWS HEX(`studio_id`) AS x_studio_id, `name`
            |FROM studio
            |LIMIT 100
          """.stripMargin
        ).apply()

        val countRow = SQL(
          """
            |SELECT FOUND_ROWS() AS total_studios
          """.stripMargin
        ).apply().head

        val totalRows = countRow[Long]("total_studios").toInt

        val studios = studioRows.map { row =>
          Studio(
            Some(ObjectID(row[String]("x_studio_id"))),
            Some(BusinessName(row[String]("name"))),
            None,
            None,
            None
          )
        }.force.toSeq

        PagedResult(studios, PageNumber(1), ResultsPerPage(100), TotalResults(totalRows))
      }
    }
  }

  def updateStudio(studio: Studio): Future[Try[Studio]] = ???

  def deleteStudio(studioId: ObjectID): Future[Try[Unit]] = Future {
    Try {
      DB.withConnection { implicit c =>
        SQL(
          """
            |DELETE
            |FROM studio
            |WHERE `studio_id` = UNHEX({studioId})
            |LIMIT 1
          """.stripMargin
        ).on("studioId" -> studioId.stripDashes).execute()
      }
    }
  }
}
