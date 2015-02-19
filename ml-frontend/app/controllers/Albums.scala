package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.mvc.Results._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import scala.concurrent.Future

import backends.{Backends, AlbumsBackendException, AlbumNotFoundException, Album}

object Albums extends Controller {
  private def actionWrap(body: SessionRequest => Future[Result])(implicit request: SessionRequest) = {
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

  def add(artistId: Long) = ProtectedAction { implicit request =>
    Future(Ok(views.html.forms.album_add(artistId)))
  }

  def albumAddForm = Form ( tuple (
    "name" -> nonEmptyText,
    "description" -> nonEmptyText,
    "year" -> number
  ))

  def performAdd(artistId: Long) = ProtectedAction { implicit request =>
    val form = albumAddForm.bindFromRequest
    form.value.map {
      case (name, description, year) =>
        val album = Album(name, description, year, artistId)
        Backends.albums.postAlbum(album).map(newAlbumId => Redirect(routes.Albums.id(newAlbumId)))
    } getOrElse {
      val errString =
        if (form.hasErrors) form.errors.map(err => s"${err.key} : ${err.message}").mkString(";")
        else "Invalid form"

      Future(BadRequest(views.html.static_pages.error_page("BAD REQUEST", errString)))
    }
  }

  def albumEditForm = Form ( tuple (
    "name" -> nonEmptyText,
    "description" -> nonEmptyText,
    "year" -> number,
    "artist-id" -> number
  ))

  def edit(id: Long) = ProtectedAction { implicit request =>
    Backends.albums.getAlbum(id).map { album =>
      Ok(views.html.forms.album_edit(id, album))
    }
  }

  def performEdit(id: Long) = ProtectedAction { implicit request =>
    val form = albumEditForm.bindFromRequest

    form.value.map {
      case (name, description, year, artistId) =>
        val album = Album(name, description, year, artistId)
        Backends.albums.editAlbum(id, album) map { success =>
          if (success) Redirect(routes.Albums.all())
          else InternalServerError(views.html.static_pages.error_page("ERROR", "Failed to edit song"))
        }
    } getOrElse {
      val errString =
        if (form.hasErrors) form.errors.map(err => s"${err.key} : ${err.message}").mkString(";")
        else "Invalid form"

      Future(BadRequest(views.html.static_pages.error_page("BAD REQUEST", errString)))
    }
  }

  def delete(id: Long) = ProtectedAction { implicit request =>
    Backends.albums.getAlbum(id) flatMap { album =>
      Backends.songs.songsOfAlbum(album.id) flatMap { songs =>
        if (songs.length == 0) Backends.albums.deleteAlbum(id).map( _ => Redirect(routes.Artists.id(album.artistId)))
        else Future(InternalServerError(views.html.static_pages.error_page("ERROR", "Cannot delete album, because there are still songs referencing it")))
      }
    } recover {
      case e: AlbumNotFoundException => InternalServerError(views.html.static_pages.error_page("ALBUM NOT FOUND", s"Failed to delete album. $e.getMessage"))
      case e: AlbumsBackendException => InternalServerError(views.html.static_pages.error_page("ERROR", s"Failed to delete album. ${e.getMessage}"))
    }
  }
}
