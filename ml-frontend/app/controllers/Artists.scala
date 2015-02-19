package controllers

import backends.{ArtistsBackend, AlbumsBackend}
import play.api.Play
import play.api.mvc._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import scala.concurrent.{ExecutionContext, Future}

import backends.{Backends, Artist, ArtistsBackendException, ArtistsNotFoundException}

object Artists extends Controller {
  def handleErrors(body: SessionRequest => Future[Result]) (implicit request: SessionRequest) = {
    body(request).recover {
      case e: ArtistsNotFoundException => NotFound(views.html.static_pages.error_page("NOT FOUND", e.getMessage))
      case e: ArtistsBackendException => InternalServerError(views.html.static_pages.error_page("INTERNAL SERVER ERROR", e.getMessage))
      case e: Exception => throw e
    }
  }

  def all = SessionAction { implicit request =>
    handleErrors { implicit request =>
      Backends.artists.getAllArtists() map { artists =>
        Ok(views.html.artists_list(artists))
      }
    }
  }

  def id(id: Long) = SessionAction { implicit request =>
    handleErrors { implicit request =>
      for {
        artist <- Backends.artists.getArtist(id)
        albums <- Backends.albums.getArtistsAlbums(id)
      } yield {
        Ok(views.html.artist(artist, albums))
      }
    }
  }

  def add() = ProtectedAction { implicit request =>
    Future(Ok(views.html.forms.artist_add()))
  }

  def artistAddForm = Form ( tuple (
    "name" -> nonEmptyText,
    "description" -> nonEmptyText
  ))

  def performAdd() = ProtectedAction { implicit request =>
    val form = artistAddForm.bindFromRequest
    form.value.map {
      case (name, description) =>
        val artist = Artist(name, description)
        Backends.artists.postArtist(artist).map(newArtistId => Redirect(routes.Artists.id(newArtistId)))
    } getOrElse {
      val errString =
        if (form.hasErrors) form.errors.map(err => s"${err.key} : ${err.message}").mkString(";")
        else "Invalid form"

      Future(BadRequest(views.html.static_pages.error_page("BAD REQUEST", errString)))
    }
  }

  def edit(id: Long) = ProtectedAction { implicit request =>
    Backends.artists.getArtist(id) map { artist =>
      Ok(views.html.forms.artist_edit(artist))
    } recover {
      case e: ArtistsNotFoundException =>
        InternalServerError(views.html.static_pages.error_page("NOT FOUND", s"e.getMessage"))
      case e: ArtistsBackendException =>
        InternalServerError(views.html.static_pages.error_page("ERROR", s"Failed to edit album. Error message: e.getMessage"))
    }
  }

  def artistEditForm = Form ( tuple (
    "name" -> nonEmptyText,
    "description" -> nonEmptyText
  ))

  def performEdit(id: Long) = ProtectedAction { implicit request =>
    val form = artistAddForm.bindFromRequest
    form.value.map {
      case (name, description) =>
        val artist = Artist(name, description)
        Backends.artists.postArtist(artist).map(newArtistId => Redirect(routes.Artists.id(newArtistId)))
    } getOrElse {
      val errString =
        if (form.hasErrors) form.errors.map(err => s"${err.key} : ${err.message}").mkString(";")
        else "Invalid form"

      Future(BadRequest(views.html.static_pages.error_page("BAD REQUEST", errString)))
    }
  }
}
