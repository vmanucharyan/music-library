package controllers

import java.time.Duration

import data.DataProvider
import models.UsersHelper
import oauth2.{AccessToken, AlphaNumericTokenGenerator, AuthInfo, AuthSessionKeeper}
import play.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

object SignIn extends Controller {
  def signIn(redirect: Option[String]) = Action { implicit request =>
    Logger.debug(s"signin: $redirect")
    Ok(views.html.login(redirect))
  }

  def signInForm = Form (
    tuple (
      "email" -> nonEmptyText(),
      "password" -> nonEmptyText()
    )
  )

  def performSignIn(redirectUri: Option[String]) = Action.async { implicit request =>
    val (m, pwd) = signInForm.bindFromRequest.get

    Logger.debug(s"$redirectUri")

    val tokenGenerator = new AlphaNumericTokenGenerator()

    DataProvider.getUserById(m) map {
      case Some(user) =>
        val pwdHash = UsersHelper.hashPassword(pwd)
        if (pwdHash == user.passHash) {
          val token = new AccessToken(m, tokenGenerator.generateToken(), Duration.ofMinutes(5), "password")

          AuthSessionKeeper.storeToken(token)

          Logger.debug(s"$redirectUri")

          redirectUri match {
            case Some(url) => Redirect(url)
              .withSession("token" -> token.value)

            case None => Redirect(routes.Application.index())
              .withSession("token" -> token.value)
          }
        }
        else BadRequest(views.html.static_pages.nosuchuser())

      case None => BadRequest(views.html.static_pages.nosuchuser())
    }
  }

  def signOut() = Action { implicit request =>
    AuthInfo.acessToken match {
      case Some(token) => AuthSessionKeeper.removeToken(token)
      case None =>
    }

    Redirect(routes.Application.index()).withNewSession
  }
}
