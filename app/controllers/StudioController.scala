package controllers

import controllers.response.{Data, Error, ResponseEnvelope}
import controllers.response.ResponseEnvelope._
import daos.StudioDao
import models.{BusinessName, Studio, ObjectID}
import models.ObjectID._
import play.api._
import play.api.libs.json.{JsValue, Writes, Json}
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

object StudioController extends Controller {
  val studioDao = new StudioDao {}

  def read(studioId: ObjectID) = Action.async {
    val ftoStudio = studioDao.readStudio(studioId)

    ftoStudio.map {
      case Failure(exception) => InternalServerError(Json.toJson(Error("The requested failed: "+exception.getMessage)))
      case Success(None) => NotFound(Json.toJson(Error("The studio could not be found ("+studioId.withDashes+")")))
      case Success(Some(studio)) => Ok(Json.toJson(Data(studio)))
    }
  }

  def index = Action.async {
    val ftStudios = studioDao.indexStudios()

    ftStudios.map {
      case  Failure(exception) => InternalServerError(Json.toJson(Error("Failed to index studios: "+exception.getMessage)))
      case Success(pagedResults) => Ok(Json.toJson(Data[Seq[Studio]](pagedResults.result)))
    }
  }

  def add = Action.async { request =>
    // Parse input
    val oJson = request.body.asJson

    val receivedJson: Either[JsValue, Result] = oJson.toLeft[Result](BadRequest(Json.toJson(Error("Request content must be JSON"))))

    val receivedStudio: Either[Studio, Result] = receivedJson.left.map { json =>
      Try(json.as[Studio])
    }.left.flatMap {
      case Failure(exception) => Right(BadRequest(Json.toJson(Error("Unable to parse studio from JSON: "+exception.getMessage))))
      case Success(studio) => Left(studio)
    }

    val createdStudio: Either[Future[Try[Studio]], Future[Result]] = receivedStudio.left.map { studio =>
      studioDao.createStudio(studio)
    }.right.map { result =>
      Future(result)
    }

    val finalResult: Either[Future[Result], Future[Result]] = createdStudio.left.map { ftStudio =>
      ftStudio.map {
        case Failure(exception) => InternalServerError(Json.toJson(Error("Failed to create studio: "+exception.getMessage)))
        case Success(studio) => Ok(Json.toJson(Data(studio)))
      }
    }

    finalResult.right.getOrElse(finalResult.left.get)
  }

  def update(studioId: ObjectID) = TODO

  def delete(studioId: ObjectID) = Action.async {
    // Ask DAO to delete studio
    studioDao.deleteStudio(studioId).map {
      case Failure(exception) => InternalServerError(Json.toJson(Error("Failed to delete studio: "+exception.getMessage)))
      case Success(deleted) =>
        if (deleted)
          Ok("")
        else
          NotFound(Json.toJson(Error("No studio with the specified ID could be found.")))
    }
  }

}
