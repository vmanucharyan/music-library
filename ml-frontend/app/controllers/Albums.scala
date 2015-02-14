package controllers

import play.api.Play
import play.api.mvc.Action
import play.mvc.Controller
import play.api.Play.current
import play.api.mvc.Results._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import backends.Backends
import backends.AlbumsBackend

object Albums extends Controller {
  def all(page: Int, pageLen: Int) = ProtectedAction { implicit request =>
    Backends.albums.all(page, pageLen) map { albums =>
      Ok(views.html.albums(albums))
    }
  }
}
