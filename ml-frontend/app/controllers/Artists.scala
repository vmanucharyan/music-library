package controllers

import backends.{ArtistsBackend, AlbumsBackend}
import play.api.Play
import play.api.mvc._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.{ExecutionContext, Future}

import backends.{Backends, ArtistsBackendException, ArtistsNotFoundException}

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
}
