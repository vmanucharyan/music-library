package controllers

import play.api.Play
import play.api.mvc.Action
import play.mvc.Controller
import play.api.Play.current
import play.api.mvc.Results._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import backends.Backends

object Songs extends Controller {
  def all(page: Int, pageLen: Int) = ProtectedAction { implicit request =>
    Backends.songs.getAllSongs(page, pageLen) map { songs =>
      Ok(views.html.songs_list(songs))
    }
  }

  def id(id: Long) = ProtectedAction { implicit request =>
    Backends.songs.getSong(id) map { song =>
      Ok(views.html.song(song))
    }
  }
}
