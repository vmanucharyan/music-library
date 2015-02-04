package backends

import play.api.Application
import play.api.http.Status
import play.api.libs.json._
import play.api.libs.ws.WS
import play.api.libs.functional.syntax._

import scala.concurrent.{ExecutionContext, Future}


class AlbumsBackend(val baseUrl: String) {
  implicit val albumReads : Reads[Album] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "description").read[String] and
    (JsPath \ "year").read[Int] and
    (JsPath \ "artist_id").read[Long] and
    (JsPath \ "id").read[Long]
  ) (Album.apply _)

  implicit val albumWrites : Writes[Album] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "description").write[String] and
    (JsPath \ "year").write[Int] and
    (JsPath \ "artist_id").write[Long] and
    (JsPath \ "id").write[Long]
  ) (unlift(Album.unapply))

  def getAlbum(id: Long) (implicit app: Application, ec: ExecutionContext) : Future[Album] =
    WS.url(s"$baseUrl/albums/$id").get().map { response =>
      response.status match {
        case Status.OK => response.json.as[Album]
        case Status.NOT_FOUND => throw new AlbumNotFoundException(s"album with id $id not found")
        case status => throw new AlbumsBackendException(s"unexpected status code $status")
      }
    }

  def postAlbum(album: Album) (implicit app: Application, ec: ExecutionContext) : Future[Unit] =
    WS.url(s"$baseUrl/albums/new").post(Json.toJson(album)) map { response =>
      response.status match {
        case Status.OK => Unit
        case status => throw new AlbumsBackendException("failed to create album")
      }
    }
}

case class Album(
  name: String,
  description: String,
  year: Int,
  artistId: Long,
  id: Long = 0
)

class AlbumsBackendException(msg: String) extends RuntimeException(msg)
class AlbumNotFoundException(msg: String) extends AlbumsBackendException(msg)
