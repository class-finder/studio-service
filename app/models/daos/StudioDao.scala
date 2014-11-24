package models.daos

import anorm._
import models.paging.{PageNumber, PagedResult, ResultsPerPage, TotalResults}
import models.{BusinessName, ObjectID, Studio}
import play.api.Play.current
import play.api.db.DB

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

trait StudioDao {
  def createStudio(studio: Studio): Future[Try[Studio]] = Future {
    Try {
      // Generate a random ID
      val studioId = ObjectID.randomID

      DB.withConnection { implicit c =>
        SQL(
          """
            |INSERT INTO studio (`studio_id`, `name`)
            |VALUES (UNHEX({studioId}), {name})
          """.stripMargin
        ).on(
          "studioId" -> studioId.stripDashes,
          "name" -> studio.name.map(_.name).orNull
        ).executeInsert()

        val studioRow = SQL(
          """
            |SELECT HEX(`studio_id`) AS x_studio_id, `name`
            |FROM studio
            |WHERE `studio_id` = UNHEX({studioId})
            |LIMIT 1
          """.stripMargin
        ).on("studioId" -> studioId.stripDashes).apply().head

        Studio(
          Some(ObjectID(studioRow[String]("x_studio_id"))),
          Some(BusinessName(studioRow[String]("name")))
        )
      }
    }
  }

  def updateStudio(studioId: ObjectID, studio: Studio): Future[Try[Option[Studio]]] = Future {
    Try {
      DB.withConnection { implicit c =>
        val numEffectedRows = SQL(
          """
            |UPDATE studio
            |SET `name` = {name}
            |WHERE `studio_id` = UNHEX({studioId})
            |LIMIT 1
          """.stripMargin
        ).on(
            "studioId" -> studioId.stripDashes,
            "name" -> studio.name.map(_.name).orNull
          ).executeUpdate()

        if (numEffectedRows != 1)
          None
        else {
          val studioRow = SQL(
            """
            |SELECT HEX(`studio_id`) AS x_studio_id, `name`
            |FROM studio
            |WHERE `studio_id` = UNHEX({studioId})
            |LIMIT 1
          """.
              stripMargin
          ).on("studioId" -> studioId.stripDashes).apply().headOption

          studioRow.map ( row =>
            Studio(
              Some(ObjectID(row[String]("x_studio_id"))),
              Some(BusinessName(row[String]("name")))
            )
          )
        }
      }
    }
  }

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

  def deleteStudio(studioId: ObjectID): Future[Try[Boolean]] = Future {
    Try {
      DB.withConnection { implicit c =>
        val rowsUpdated = SQL(
          """
            |DELETE
            |FROM studio
            |WHERE `studio_id` = UNHEX({studioId})
            |LIMIT 1
          """.stripMargin
        ).on("studioId" -> studioId.stripDashes).executeUpdate()

        rowsUpdated == 1
      }
    }
  }
}
