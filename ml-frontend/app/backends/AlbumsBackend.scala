package backends

import play.api.Application
import play.api.http.Status
import play.api.libs.json._
import play.api.libs.ws._
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

  def all(page: Int, pageLen: Int)(implicit app: Application, ec: ExecutionContext) : Future[List[Album]] =
    WS.url(s"$baseUrl/albums")
      .withQueryString("page" -> s"$page", "page_len" -> s"$pageLen")
      .get() map { response =>
        response.status match {
          case Status.OK => (response.json \ "values").as[List[Album]]
          case status => throw new AlbumsBackendException(s"unexpected status code ($status)")
        }
      }

  def getArtistsAlbums(artistId: Long) (implicit app: Application, ec: ExecutionContext) : Future[List[Album]] =
    WS.url(s"$baseUrl/albums/of_artist/$artistId").get() map { response =>
      response.status match {
        case Status.OK => (response.json \ "values").as[List[Album]]
        case status => throw new AlbumsBackendException(s"unexpected status code ($status)")
      }
    }

  def getAlbum(id: Long) (implicit app: Application, ec: ExecutionContext) : Future[Album] =
    WS.url(s"$baseUrl/albums/$id").get() map { response =>
      response.status match {
        case Status.OK => response.json.as[Album]
        case Status.NOT_FOUND => throw new AlbumNotFoundException(s"album with id $id not found")
        case status => throw new AlbumsBackendException(s"unexpected status code ($status)")
      }
    }

  def albumExists(id: Long) (implicit app: Application, ec: ExecutionContext) : Future[Boolean] =
    getAlbum(id)
      .map(_ => true)
      .recover {
        case e: AlbumNotFoundException => false
        case e: Exception => throw e
      }

  def postAlbum(album: Album) (implicit app: Application, ec: ExecutionContext) : Future[Long] =
    WS.url(s"$baseUrl/albums/new").post(Json.toJson(album)) map { response =>
      response.status match {
        case Status.OK => (response.json \ "id").as[Long]
        case status => throw new AlbumsBackendException("failed to create album")
      }
    }

  def editAlbum(id: Long, album: Album) (implicit app: Application, ec: ExecutionContext) : Future[Boolean] =
    WS.url(s"$baseUrl/albums/$id/update").put(Json.toJson(album)) map { response =>
      response.status match {
        case Status.OK => true
        case Status.NOT_FOUND => throw new AlbumNotFoundException(s"album with id $id not found")
        case status => throw new AlbumsBackendException(s"failed to edit album: unexpected status code ($status)")
      }
    }

  def deleteAlbum(id: Long) (implicit app: Application, ec: ExecutionContext) : Future[Unit] =
    WS.url(s"$baseUrl/albums/$id/delete").delete() map { response =>
      response.status match {
        case Status.OK => Unit
        case status =>
          val errorMessage = (response.json \ "error").as[String]
          throw new AlbumsBackendException(s"unexpected status code ($status). error message: ${errorMessage}")
      }
    }

  def countArtistsAlbums(albumId: Long)(implicit app: Application, ec: ExecutionContext) : Future[Int] =
    getArtistsAlbums(albumId).map(albums => albums.length)
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
