package controllers

import data.DataProvider
import models._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

object Register extends Controller {
  def register = Action { implicit request =>
    Ok(views.html.register())
  }

  def registerForm = Form ( tuple (
    "email" -> nonEmptyText(3, 255),
    "full_name" -> nonEmptyText(1, 255),
    "password" -> nonEmptyText(3, 20)
  ))

  def performRegister = Action.async { implicit rs =>
    val (email, fullName, pass) = registerForm.bindFromRequest.get
    val passHash = UsersHelper.hashPassword(pass)
    val user = User(email, fullName, passHash)

    DataProvider.insertUser(user).map { e =>
      Redirect(routes.Application.index())
    }
  }
}
