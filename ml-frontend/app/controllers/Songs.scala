package controllers

import play.api.Play
import play.api.mvc.Action
import play.mvc.Controller
import play.api.Play.current
import play.api.mvc.Results._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import scala.concurrent.Future

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import backends.Backends
import backends.Song
import backends.{SongsBackendException, SongNotFoundException}

object Songs extends Controller {
  def all(page: Int, pageLen: Int) = ProtectedAction { implicit request =>
    Backends.songs.getAllSongs(page, pageLen) map { songs =>
      Ok(views.html.songs_list(songs, page, pageLen))
    }
  }

  def id(id: Long) = ProtectedAction { implicit request =>
    val aggregation = for {
      song <- Backends.songs.getSong(id)
      album <- Backends.albums.getAlbum(song.albumId)
      artist <- Backends.artists.getArtist(song.artistId)
    } yield (song, album, artist)

    aggregation map {
      case (song, album, artist) => Ok(views.html.song(song, album, artist))
    } recover {
      case e: SongNotFoundException => NotFound(views.html.static_pages.error_page("NOT FOUND", e.getMessage))
      case e: Exception => InternalServerError(views.html.static_pages.error_page("ERROR", e.getMessage))
    }
  }

  def newSong(albumId: Long, artistId: Long) = ProtectedAction { implicit request =>
    Future(Ok(views.html.forms.song_add(albumId, artistId)))
  }

  def deleteSong(id: Long) = ProtectedAction { implicit request =>
    Backends.songs.deleteSong(id) map { _ =>
      Redirect(routes.Application.index())
    }
  }

  def editSong(id: Long) = ProtectedAction { implicit request =>
    Backends.songs.getSong(id) map { song =>
        Ok(views.html.forms.song_edit(song))
    } recover {
      case e: Exception => InternalServerError(views.html.static_pages.error_page("ERROR", e.getMessage))
    }
  }

  def songAddForm = Form ( tuple (
    "name" -> nonEmptyText(1, 255),
    "genre" -> nonEmptyText(1, 255),
    "duration" -> number
  ))

  def performAdd(albumId: Long, artistId: Long) = ProtectedAction { implicit request =>
    val form = songAddForm.bindFromRequest
    form.value.map {
      case (name, genre, durationSec) =>
        val newSong = Song(name, genre, durationSec, albumId, artistId)
        Backends.songs.postSong(newSong) map { songId =>
          Redirect(routes.Songs.id(songId))
        } recover {
          case e: SongsBackendException => InternalServerError(views.html.static_pages.error_page("INTERNAL SERVER ERROR", s"Failed to add song. Error message: $e.getMessage"))
          case e => throw e
        }

    } getOrElse {
      val errString =
        if (form.hasErrors) (for (err <- form.errors) yield s"${err.key} : ${err.message}").mkString(";")
        else "Invalid form"

      Future(BadRequest(views.html.static_pages.error_page("BAD REQUEST", errString)))
    }
  }

  def songEditForm = Form ( tuple (
    "name" -> nonEmptyText(1, 255),
    "genre" -> nonEmptyText(1, 255),
    "duration" -> number,
    "album-id" -> number,
    "artist-id" -> number
  ))

  def performEdit(songId: Long) = ProtectedAction { implicit request =>
    val form = songEditForm.bindFromRequest
    form.value.map {

      case (name, genre, durationSec, albumId, artistId) =>
        val editedSong = Song(name, genre, durationSec, albumId, artistId)
        Backends.songs.editSong(songId, editedSong).map( _ => Redirect(routes.Songs.id(songId)))

    } getOrElse {
      val errString =
        if (form.hasErrors) form.errors.map(err => s"${err.key} : ${err.message}").mkString(";")
        else "Invalid form"

      Future(BadRequest(views.html.static_pages.error_page("BAD REQUEST", errString)))
    }
  }
}
