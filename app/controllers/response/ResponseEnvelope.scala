package controllers.response

import models.Studio
import play.api.libs.json._
import play.api.libs.functional.syntax._

object ResponseEnvelope {
  implicit def responseEnvelopeWrites[T](implicit tWrites: Writes[T]): Writes[ResponseEnvelope[T]] = new Writes[ResponseEnvelope[T]] {
    def writes(envelope: ResponseEnvelope[T]) = Json.obj(
      "error" -> envelope.error,
      "data" -> envelope.data
    )
  }

  def Data[T](data: T) = ResponseEnvelope(Some(data), None)

  def Error(error: String) = ResponseEnvelope[Int](None, Some(error))
}

case class ResponseEnvelope[T](data: Option[T], error: Option[String])
