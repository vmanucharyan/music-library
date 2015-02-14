package controllers

import data.DataProvider
import models.oauth2.OAuthApp
import oauth2.{RandomAppCredsGenerator}
import play.api.Application
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

import play.api.Play.current

import scala.concurrent.{ExecutionContext, Future}

object Users extends Controller {
  def all() = SessionAction { implicit rs =>
      data.DataProvider.getUsers().map(users => Ok(views.html.users(users)))
  }

  def me() = ProtectedAction { implicit rs =>
    rs.sessionInfo.map { sessionInfo => 
      for {
        user <- DataProvider.getUserById(sessionInfo.userId)
        apps <- DataProvider.getUserApps(sessionInfo.userId)
      } yield {
        Ok(views.html.user_page(user.get, apps))
      }
    }
    .getOrElse(Future(Unauthorized("Unauthorized")))
  }
}
