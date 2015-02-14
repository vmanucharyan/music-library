package backends

import play.api.Play
import play.api.Play.current

object Backends {
  val session = new SessionBackend(Play.application.configuration.getString("session_backend_url").get)
  val albums = new AlbumsBackend(Play.application.configuration.getString("albums_backend_url").get)
  val artists = new ArtistsBackend(Play.application.configuration.getString("artists_backend_url").get)
  val songs = new SongsBackend(Play.application.configuration.getString("songs_backend_url").get)
}
