package controllers

import daos.StudioDao
import models.ObjectID
import models.ObjectID._
import play.api._
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}

object StudioController extends Controller with StudioDao {

  def read(studioId: ObjectID) = Action.async {
    val ftoStudio = readStudio(studioId)

    ftoStudio.map {
      case Failure(exception) => BadRequest("The requested failed: "+exception.getMessage)
      case Success(None) => NotFound("The studio could not be found ("+studioId.withDashes+")")
      case Success(Some(studio)) => Ok(studio.toString)
    }


  }

  def add = TODO

}
