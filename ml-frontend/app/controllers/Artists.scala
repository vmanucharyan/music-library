package controllers

import backends.{ArtistsBackend, AlbumsBackend}
import play.api.Play
import play.api.mvc._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.{ExecutionContext, Future}


object Artists extends Controller {
  val albumsBackend = new AlbumsBackend(Play.application.configuration.getString("albums_backend_url").get)
  val artistsBackend = new ArtistsBackend(Play.application.configuration.getString("artists_backend_url").get)

  def all = SessionAction { implicit request =>
    artistsBackend.getAllArtists() map { artists =>
      Ok(views.html.artists_all(artists))
    }
  }
}
