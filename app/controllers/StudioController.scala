package controllers

import controllers.response.{Data, Error, ResponseEnvelope}
import controllers.response.ResponseEnvelope._
import daos.StudioDao
import models.{Studio, ObjectID}
import models.ObjectID._
import play.api._
import play.api.libs.json.{Writes, Json}
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

  def add = TODO

  def delete(studioId: ObjectID) = Action.async {
    // Ask DAO to delete studio
    studioDao.deleteStudio(studioId).map {
      case Failure(exception) => InternalServerError(Json.toJson(Error("Failed to delete studio: "+exception.getMessage)))
      case Success(_) => Ok("")
    }
  }

}
