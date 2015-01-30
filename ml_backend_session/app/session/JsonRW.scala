package session

import play.api.libs.json._
import play.api.libs.functional.syntax._

object JsonRW {
  implicit val sessionReads : Reads[Session] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "user_id").read[String] and
    (JsPath \ "auth_token").read[String]
  ) (Session.apply _)

  implicit val sessionWrites: Writes[Session] = (
    (JsPath \ "id").write[String] and
    (JsPath \ "user_id").write[String] and
    (JsPath \ "auth_token").write[String]
  ) (unlift(Session.unapply))
}
