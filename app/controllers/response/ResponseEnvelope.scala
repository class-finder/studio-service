package controllers.response

import models.Studio
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._
import play.api.libs.functional.syntax._

object ResponseEnvelope {
  implicit def responseEnvelopeWrites[T](implicit tWrites: Writes[T]): Writes[ResponseEnvelope[T]] = new Writes[ResponseEnvelope[T]] {
    def writes(envelope: ResponseEnvelope[T]) = envelope match {
      case Data(data) => Json.obj("data" -> data)
      case Error(error) => Json.obj("error" -> error)
    }
  }
}

abstract class ResponseEnvelope[T](data: Option[T], error: Option[String]) {
  val isError: Boolean = error.isDefined
}

case class Data[T](data: T) extends ResponseEnvelope(Some(data), None)

case class Error(error: String) extends ResponseEnvelope[Int](None, Some(error))
