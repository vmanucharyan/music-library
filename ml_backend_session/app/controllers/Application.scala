package controllers

import play.api.cache.Cache
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current

import session.JsonRW._
import session.Session

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def newSession() = Action { implicit request =>
    request.body.asJson match {

      case Some(jsValue) =>
        val session = Json.fromJson[Session](jsValue)
        Cache.set(session.id, session)
        Ok(Json.obj("message" -> "success"))

      case None => BadRequest(Json.obj("error" -> "no json in body"))
    }
  }

  def getSession(id: String) = Action {
    Cache.getAs[Session](id) match {
      case Some(session) => Ok(Json.toJson[Session](session))
      case None => NotFound(Json.obj("error" -> "session not found"))
    }
  }

  def deleteSession(id: String) = Action {
    Cache.remove(id)
    Ok("message" -> "success")
  }

}
