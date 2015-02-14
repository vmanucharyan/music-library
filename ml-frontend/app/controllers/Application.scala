package controllers

import play.api._
import play.api.mvc._
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import play.api.mvc.Result
import play.api.mvc.Result._

import scala.concurrent.Future

import backends._
import oauth2.AuthAction

object Application extends Controller {
  val sessionBackend = new SessionBackend(Play.application.configuration.getString("session_backend_url").get)
  val albumsBackend = new AlbumsBackend(Play.application.configuration.getString("albums_backend_url").get)
  val artistsBackend = new ArtistsBackend(Play.application.configuration.getString("artists_backend_url").get)
  val songsBackend = new SongsBackend(Play.application.configuration.getString("songs_backend_url").get)

  implicit val sessionBackendImplicit = sessionBackend

  def index = SessionAction { implicit request =>
    Future(Ok(views.html.index()))
  }

  def sessionTest = SessionAction { implicit request =>
    for {
      id <- sessionBackend.newSession(new SessionInfo("user@example.com", "adfsdfs"))
      getSession <- sessionBackend.getSession(id)
      deleteSession <- sessionBackend.deleteSession(id)
    } yield {
      Ok(s"created session id: $id\n\n" +
         s"received session: $getSession")
    }
  }

  def albumTest = ProtectedAction { implicit request =>
    for {
      album1 <- albumsBackend.getAlbum(1)
      addedAlbumId <- albumsBackend.postAlbum(Album(name = "new_album", description = "desc", year = 1111, artistId = 1))
      addedAlbum <- albumsBackend.getAlbum(addedAlbumId)
      _ <- albumsBackend.editAlbum(addedAlbumId, Album(name = "new_album EDITED!!", description = "desc", year = 1234, artistId = 1))
      editedAlbum <- albumsBackend.getAlbum(addedAlbumId)
    } yield {
      Logger.info(s"${request.sessionInfo}")
 //     val userId = request.sessionInfo.userId.getOrElse("")
      Ok(//s"USER: $userId\n\n" +
         s"album 1:\n$album1\n\n" +
         s"added album id: $addedAlbumId\n\n" +
         s"added album: $addedAlbum\n\n" +
         s"edited album:\n$editedAlbum")
    }
  }

  def artistTest = SessionAction { implicit request =>
    for {
      artist1 <- artistsBackend.getArtist(1)
      addedArtistId <- artistsBackend.postArtist(Artist(name = "new artist", description = "desc"))
      addedArtist <- artistsBackend.getArtist(addedArtistId)
      _ <- artistsBackend.editArtist(addedArtistId, Artist(name = "new artist EDITED!!", description = "desc"))
      editedArtist <- artistsBackend.getArtist(addedArtistId)
    } yield {
      Ok(s"album 1:\n$artist1\n\n" +
         s"added artist id: $addedArtistId\n\n" +
         s"added artist: $addedArtist\n\n" +
         s"edited artist:\n$editedArtist")
    }
  }

  def songsTest = SessionAction { implicit request =>
    for {
      song3 <- songsBackend.getSong(3)
      addedSongId <- songsBackend.postSong(Song(name = "new song", durationSec = 100, genre = "genre", albumId = 1, artistId = 1))
      addedSong <- songsBackend.getSong(addedSongId)
      _ <- songsBackend.editSong(addedSongId, Song(name = "new song EDITED!!!", durationSec = 123, genre = "genre EDITED!!", albumId = 1, artistId = 1))
      editedSong <- songsBackend.getSong(addedSongId)
      ofArtist1 <- songsBackend.songsOfArtist(1)
      ofAlbum1 <- songsBackend.songsOfAlbum(1)
    } yield {
      Ok(s"song 3:\n$song3\n\n" +
         s"added song id: $addedSongId\n\n" +
         s"added song: $addedSong\n\n" +
         s"edited song:\n$editedSong\n\n" +
         s"of artist 1:\n$ofArtist1\n\n" +
         s"of album 1:\n$ofAlbum1")
    }
  }
}
