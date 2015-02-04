package controllers

import backends.{SessionInfo, SessionBackend}
import play.api._
import play.api.mvc._
import play.api.Play
import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {
  val sessionBackend = new SessionBackend(Play.application.configuration.getString("session_backend_url").get)

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def sessionTest = Action.async {
    for {
      id <- sessionBackend.newSession(new SessionInfo("user@example.com", "adfsdfs"))
      getSession <- sessionBackend.getSession(id)
      deleteSession <- sessionBackend.deleteSession(id)
    } yield {
      val str = s"created session id: $id\n" +
                s"received session $getSession\n"

      Ok(str)
    }
  }
}
