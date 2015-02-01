package session

import play.api.libs.json._
import play.api.libs.functional.syntax._

object JsonRW {
  implicit val sessionReads : Reads[Session] = (
    (JsPath \ "user_id").read[String] and
    (JsPath \ "auth_token").read[String] and
    (JsPath \ "id").readNullable[String]
  ) (Session.apply _)

  implicit val sessionWrites: Writes[Session] = (
    (JsPath \ "user_id").write[String] and
    (JsPath \ "auth_token").write[String] and
    (JsPath \ "id").writeNullable[String]
  ) (unlift(Session.unapply))
}
