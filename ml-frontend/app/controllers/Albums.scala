package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.mvc.Results._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

import backends.Backends
import backends.AlbumsBackendException

object Albums extends Controller {
  private def actionWrap(body: SessionRequest => Future[Result]) (implicit request: SessionRequest) = {
    body(request).recover {
      case e: AlbumsBackendException => InternalServerError(views.html.static_pages.error_page("INTERNAL SERVER ERROR", e.getMessage))
      case e => throw e
    }
  }

  def all(page: Int, pageLen: Int) = ProtectedAction { implicit request =>
    actionWrap { implicit request =>
      Backends.albums.all(page, pageLen) map { albums =>
        Ok(views.html.albums_list(albums))
      }
    }
  }

  def id(id: Long) = ProtectedAction { implicit request => 
    actionWrap { implicit request =>
      for {
        album <- Backends.albums.getAlbum(id)
        songs <- Backends.songs.songsOfAlbum(id)
        artist <- Backends.artists.getArtist(album.artistId)
      } yield {
        Ok(views.html.album(album, songs, artist))
      }
    }
  }
}
