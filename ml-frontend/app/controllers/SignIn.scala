package controllers

import java.time.Duration

import data.DataProvider
import models.UsersHelper
import play.api.Play.current
import oauth2.{AccessToken, AlphaNumericTokenGenerator, AuthSessionKeeper}
import play.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

import backends.SessionInfo
import backends.Backends

object SignIn extends Controller {
  def signIn(redirect: Option[String]) = SessionAction { implicit request =>
    Logger.debug(s"signin: $redirect")
    Future(Ok(views.html.login(redirect)))
  }

  def signInForm = Form (
    tuple (
      "email" -> nonEmptyText(),
      "password" -> nonEmptyText()
    )
  )

  def performSignIn(redirectUri: Option[String]) = SessionAction { implicit request =>
    val (m, pwd) = signInForm.bindFromRequest.get
    val userFuture = DataProvider.getUserById(m)

    userFuture flatMap { userOpt =>
      userOpt map { user =>
        val sessionInfo = new SessionInfo(userId = user.email, authToken = new AlphaNumericTokenGenerator().generateToken())
        val sessionIdFuture = Backends.session.newSession(sessionInfo)

        sessionIdFuture map { sessionId =>
          Redirect(redirectUri.map(uri => uri).getOrElse(routes.Application.index().url))
            .withSession("session_id" -> sessionId, "auth_token" -> sessionInfo.authToken)
        }

      } getOrElse(Future(Forbidden(views.html.static_pages.nosuchuser())))
    }
  }

  def signOut() = ProtectedAction { implicit request =>
    request.sessionInfo.map(session => Backends.session.deleteSession(session.id.getOrElse("")))
    Future(Redirect(routes.Application.index()).withNewSession)
  }
}
