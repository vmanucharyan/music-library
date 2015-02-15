package backends

import play.api.Application
import play.api.http.Status
import play.api.Play
import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.libs.functional.syntax._
import scala.concurrent.{Future, ExecutionContext}

class SessionBackend(val baseUrl: String) {
  implicit val sessionReads : Reads[SessionInfo] = (
      (JsPath \ "user_id").read[String] and
      (JsPath \ "auth_token").read[String] and
      (JsPath \ "id").readNullable[String]
    ) (SessionInfo.apply _)

  implicit val sessionWrites: Writes[SessionInfo] = (
      (JsPath \ "user_id").write[String] and
      (JsPath \ "auth_token").write[String] and
      (JsPath \ "id").writeNullable[String]
    ) (unlift(SessionInfo.unapply))

  def getSession(id: String) (implicit app: Application, ec: ExecutionContext): Future[Option[SessionInfo]] = {
    val fullUrl = s"$baseUrl/session/$id"

    WS.url(fullUrl)
      .withQueryString("id" -> id)
      .get() map { response =>
        response.status match {
          case Status.OK => Json.fromJson[SessionInfo](response.json).asOpt
          case Status.NOT_FOUND => None
          case status =>
            val errorMessage = (response.json \ "error").as[String]
            throw new SessionBackendException(s"unexpected status code ($status) returned from session " +
                                              s"backend at $fullUrl. error message: $errorMessage")
        }
      }
  }

  def newSession(session: SessionInfo) (implicit app: Application, ec: ExecutionContext) : Future[String] = {
    val fullUrl = s"$baseUrl/sessions/new"

    WS.url(fullUrl)
      .post(Json.toJson(session)) map { response =>
        response.status match {
          case Status.OK => (response.json \ "id").as[String]
          case status =>
            val errorMessage = (response.json \ "error").as[String]
            throw new SessionBackendException(s"unexpected status code ($status) returned from session " +
                                              s"backend at $fullUrl. error message: $errorMessage")
        }
      }
  }

  def deleteSession(id: String) (implicit app: Application, ec: ExecutionContext) : Future[Unit] = {
    val fullUrl = s"$baseUrl/session/$id/delete"

    WS.url(fullUrl)
      .execute("DELETE") map { response =>
        response.status match {
          case Status.OK => Unit
          case status =>
            val errorMessage = (response.json \ "error").as[String]
            throw new SessionBackendException(s"unexpected status code ($status) returned from session " +
                                              s"backend at $fullUrl. error message: $errorMessage")
        }
      }
  }
}

case class SessionInfo(userId: String, authToken: String, id: Option[String] = None)

class SessionBackendException(what: String) extends RuntimeException(what)