package controllers.response

import models.Studio
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._
import play.api.libs.functional.syntax._

object ResponseEnvelope {
  implicit def responseEnvelopeWrites[T](implicit tWrites: Writes[T]): Writes[ResponseEnvelope[T]] = new Writes[ResponseEnvelope[T]] {
    def writes(envelope: ResponseEnvelope[T]) = envelope match {
      case DataResponse(data) => Json.obj("data" -> data)
      case EmptyResponse => Json.obj()
      case ErrorResponse(error) => Json.obj("error" -> error)
    }
  }
}

sealed abstract class ResponseEnvelope[T](data: Option[T], error: Option[String]) {
  val isError: Boolean = error.isDefined
}

case object EmptyResponse extends ResponseEnvelope[Int](None, None)

case class DataResponse[T](data: T) extends ResponseEnvelope(Some(data), None)

case class ErrorResponse(error: String) extends ResponseEnvelope[Int](None, Some(error))
