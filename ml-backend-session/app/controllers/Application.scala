package controllers

import play.api.cache.Cache
import play.api.mvc._
import play.api.libs.json._
import play.api.Play.current

import session.JsonRW._
import session.{SessionKeeper, RandomSessionIdGenerator, Session}

object Application extends Controller {
  val sessionIdGenerator = new RandomSessionIdGenerator()
  val sessionKeeper = new SessionKeeper()

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def newSession() = Action { implicit request =>
    request.body.asJson match {

      case Some(jsValue) =>
        Json.fromJson[Session](jsValue).asOpt match {

          case Some(session) =>
            val newSession = session.copy(id = Some(sessionIdGenerator.generate()))
            sessionKeeper.storeSession(newSession)
            Ok(Json.obj("message" -> "success", "id" -> newSession.id))

          case None => BadRequest(Json.obj("error" -> "invalid json"))
        }

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
    Ok(Json.obj("message" -> "success"))
  }

}
