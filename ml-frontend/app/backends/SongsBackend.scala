package backends

import play.api.Application
import play.api.http.Status
import play.api.libs.json.{Json, Writes, JsPath, Reads}
import play.api.libs.functional.syntax._
import play.api.libs.ws.WS

import scala.concurrent.{Future, ExecutionContext}

class SongsBackend(val baseUrl: String) {
  import Song.{reads, writes}

  def getAllSongs(page: Int, pageLen: Int) (implicit app: Application, ec: ExecutionContext) : Future[List[Song]] =
    WS.url(s"$baseUrl/songs")
      .withQueryString("page" -> s"$page", "page_len" -> s"$pageLen")
      .get().map { implicit response =>
        response.status match {
          case Status.OK => (response.json \ "values").as[List[Song]]
          case status => throw new SongsBackendException(s"unexpected status code ${status}")
      }
    }

  def getSong(id: Long) (implicit app: Application, ec: ExecutionContext) : Future[Song] =
    WS.url(s"$baseUrl/songs/$id").get() map { response =>
      response.status match {
        case Status.OK => response.json.as[Song]
        case Status.NOT_FOUND => throw new SongNotFoundException(s"song with id $id not found")
        case status => throw new SongsBackendException(s"unexpected status code ($status)")
      }
    }

  def songsOfAlbum(albumId: Long) (implicit app: Application, ec: ExecutionContext) : Future[List[Song]] =
    WS.url(s"$baseUrl/songs/of_album/$albumId").get() map { response =>
      response.status match {
        case Status.OK => (response.json \ "values").as[List[Song]]
        case status => throw new SongsBackendException(s"unexpected status code ($status)")
      }
    }

  def songsOfArtist(artistId: Long) (implicit app: Application, ec: ExecutionContext) : Future[List[Song]] =
    WS.url(s"$baseUrl/songs/of_artist/$artistId").get() map { response =>
      response.status match {
        case Status.OK => (response.json \ "values").as[List[Song]]
        case status => throw new SongsBackendException(s"unexpected status code ($status)")
      }
    }

  def postSong(song: Song) (implicit app: Application, ec: ExecutionContext) : Future[Long] =
    WS.url(s"$baseUrl/songs/new").post(Json.toJson(song)) map { response =>
      response.status match {
        case Status.OK => (response.json \ "id").as[Long]
        case status => throw new SongsBackendException("failed to create song")
      }
    }

  def editSong(id: Long, song: Song) (implicit app: Application, ec: ExecutionContext) : Future[Boolean] =
    WS.url(s"$baseUrl/songs/$id/update").put(Json.toJson(song)) map { response =>
      response.status match {
        case Status.OK => true
        case Status.NOT_FOUND => throw new SongNotFoundException(s"song with id $id not found")
        case status => throw new SongsBackendException(s"failed to edit song: unexpected status code ($status)")
      }
    }

  def deleteSong(id: Long) (implicit app: Application, ec: ExecutionContext) : Future[Unit] =
    WS.url(s"$baseUrl/song/$id/delete").delete() map { response =>
      response.status match {
        case Status.OK => Unit
        case status => throw new SongsBackendException(s"unexpected status code ($status)")
      }
    }
}

case class Song (
  name: String,
  genre: String,
  durationSec: Int,
  albumId: Long,
  artistId: Long,
  id: Long = 0
)

object Song {
  implicit val reads: Reads[Song] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "genre").read[String] and
    (JsPath \ "duration_sec").read[Int] and
    (JsPath \ "album_id").read[Long] and
    (JsPath \ "artist_id").read[Long] and
    (JsPath \ "id").read[Long]
  ) (Song.apply _)

  implicit val writes: Writes[Song] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "genre").write[String] and
    (JsPath \ "duration_sec").write[Int] and
    (JsPath \ "album_id").write[Long] and
    (JsPath \ "artist_id").write[Long] and
    (JsPath \ "id").write[Long]
  ) (unlift(Song.unapply))
}

class SongsBackendException(msg: String) extends RuntimeException(msg)
class SongNotFoundException(msg: String) extends SongsBackendException(msg)