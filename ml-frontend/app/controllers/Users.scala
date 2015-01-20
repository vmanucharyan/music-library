package controllers

import data.DataProvider
import models.oauth2.OAuthApp
import oauth2.{AuthInfo, RandomAppCredsGenerator}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Users extends Controller {
  def all = Action.async { implicit rs =>
    if (AuthInfo.isAuthorized) {
      data.DataProvider.getUsers()
        .map(users => Ok(views.html.users(users)))
    }
    else Future(Redirect(routes.SignIn.signIn()))
  }

  def me = Action.async { implicit rs =>
    AuthInfo.acessToken match {
      case Some(token) =>
        for {
          user <- DataProvider.getUserById(token.userId)
          apps <- DataProvider.getUserApps(token.userId)
        } yield {
          Ok(views.html.user_page(user.get, apps))
        }
      case None =>
        Future(Redirect(routes.SignIn.signIn()))
    }
  }

  def registerApp = Action.async { implicit rs =>
    AuthInfo.acessToken match {
      case Some(token) =>
        val appCredsGenerator = new RandomAppCredsGenerator()
        val id = appCredsGenerator.generateId()
        val secret = appCredsGenerator.generateKey()

        DataProvider.insertApplication(new OAuthApp(id, secret, token.userId)).map { f =>
          Redirect(routes.Users.me())
        }

      case None => Future(Redirect(routes.SignIn.signIn(Some(routes.Users.registerApp().absoluteURL()))))
    }
  }
}
