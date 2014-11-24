package controllers

import controllers.response.{EmptyResponse, DataResponse, ErrorResponse, ResponseEnvelope}
import controllers.response.ResponseEnvelope._
import models.daos.StudioDao
import models.{BusinessName, Studio, ObjectID}
import models.ObjectID._
import play.api._
import play.api.libs.json.{JsValue, Writes, Json}
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

object StudioController extends Controller {
  def read(studioId: ObjectID) = Action.async {
    val ftoStudio = Studio.load(studioId)

    ftoStudio.map {
      case Failure(exception) => InternalServerError(Json.toJson(ErrorResponse("The requested failed: "+exception.getMessage)))
      case Success(None) => NotFound(Json.toJson(ErrorResponse("The studio could not be found ("+studioId.withDashes+")")))
      case Success(Some(studio)) => Ok(Json.toJson(DataResponse(studio)))
    }
  }

  def index = Action.async {
    val ftStudios = Studio.all

    ftStudios.map {
      case  Failure(exception) => InternalServerError(Json.toJson(ErrorResponse("Failed to index studios: "+exception.getMessage)))
      case Success(studios) => Ok(Json.toJson(DataResponse[Seq[Studio]](studios)))
    }
  }

  def add = Action.async { request =>
    // Grab JSON input
    val oJson = request.body.asJson
    val receivedJson: Either[JsValue, Result] = oJson.toLeft[Result](BadRequest(Json.toJson(ErrorResponse("Request content must be JSON"))))


    // Attempt to parse out a Studio object
    val receivedStudio: Either[Studio, Result] = receivedJson.left.map { json =>
      Try(json.as[Studio])
    }.left.flatMap {
      case Failure(exception) => Right(BadRequest(Json.toJson(ErrorResponse("Unable to parse studio from JSON: "+exception.getMessage))))
      case Success(studio) => Left(studio)
    }

    // Send the studio to the DB
    val createdStudio: Either[Future[Try[Option[Studio]]], Result] = receivedStudio.left.map { studio =>
      studio.save
    }

    // Validate the DB insert was successful
    val fResult: Future[Result] = createdStudio.left.map { ftStudio =>
      ftStudio.map {
        case Failure(exception) => InternalServerError(Json.toJson(ErrorResponse("Failed to create studio: "+exception.getMessage)))
        case Success(Some(studio)) => Ok(Json.toJson(DataResponse(studio)))
      }
    }.fold(l => l, r => Future(r))

    fResult
  }

  def update(studioId: ObjectID) = Action.async { request =>
    // Grab JSON input
    val oJson = request.body.asJson
    val receivedJson: Either[JsValue, Result] = oJson.toLeft[Result](BadRequest(Json.toJson(ErrorResponse("Request content must be JSON"))))

    // Attempt to parse out a Studio object
    val receivedStudio: Either[Studio, Result] = receivedJson.left.map { json =>
      Try(json.as[Studio])
    }.left.flatMap {
      case Failure(exception) => Right(BadRequest(Json.toJson(ErrorResponse("Unable to parse studio from JSON: "+exception.getMessage))))
      case Success(studio) => Left(studio)
    }

    // Send the studio to the DB
    val updatedStudio: Either[Future[Try[Option[Studio]]], Result] = receivedStudio.left.map { studio =>
      studio.copy(studioId = Some(studioId)).save
    }

    // Validate the DB insert was successful
    val fResult: Future[Result] = updatedStudio.left.map { ftStudio =>
      ftStudio.map {
        case Failure(exception) => InternalServerError(Json.toJson(ErrorResponse("Failed to update studio: "+exception.getMessage)))
        case Success(None) => NotFound(Json.toJson(ErrorResponse("A studio with the specified ID could not be found")))
        case Success(Some(studio)) => Ok(Json.toJson(DataResponse(studio)))
      }
    }.fold(l => l, r => Future(r))

    fResult
  }

  def delete(studioId: ObjectID) = Action.async {
    // Ask DAO to delete studio
    Studio.delete(studioId).map {
      case Failure(exception) => InternalServerError(Json.toJson(ErrorResponse("Failed to delete studio: "+exception.getMessage)))
      case Success(deleted) =>
        if (deleted)
          Ok(Json.toJson(EmptyResponse))
        else
          NotFound(Json.toJson(ErrorResponse("No studio with the specified ID could be found.")))
    }
  }

}
