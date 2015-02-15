package controllers

import play.api.mvc._
import play.api.mvc.Results._
import play.api._
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import backends.{SessionBackend, SessionInfo, Backends}

class SessionRequest(val sessionInfo: Option[SessionInfo], request: Request[AnyContent]) extends WrappedRequest[AnyContent](request)

object SessionAction {
  def apply(body: SessionRequest => Future[Result]) = Action.async { implicit request =>
    val sessionFuture : Future[Option[SessionInfo]] = 
      request.session.get("session_id")
        .map(ssid => Backends.session.getSession(ssid))
        .getOrElse(Future(None))

    sessionFuture.flatMap(s => body(new SessionRequest(s, request)))
  }
}

object ProtectedAction {
  def apply(body: SessionRequest => Future[Result]) = SessionAction { implicit request =>
    request.sessionInfo
      .map(sessionInfo => body(request))
      .getOrElse(Future(Redirect(routes.SignIn.signIn(Some(request.uri)))))
  }
}
