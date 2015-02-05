package backends

import play.api.Application
import play.api.http.Status
import play.api.libs.json.{Json, Writes, JsPath, Reads}
import play.api.libs.functional.syntax._
import Artist._
import play.api.libs.ws.WS

import scala.concurrent.{Future, ExecutionContext}

class ArtistsBackend(val baseUrl: String) {

  def getArtist(id: Long) (implicit app: Application, ec: ExecutionContext) : Future[Artist] =
    WS.url(s"$baseUrl/artists/$id").get() map { response =>
      response.status match {
        case Status.OK => response.json.as[Artist]
        case Status.NOT_FOUND => throw new ArtistsNotFoundException(s"artist with id $id not found")
        case status => throw new ArtistsBackendException(s"unexpected status code ($status)")
      }
    }

  def postArtist(artist: Artist) (implicit app: Application, ec: ExecutionContext) : Future[Long] =
    WS.url(s"$baseUrl/artists/new").post(Json.toJson(artist)) map { response =>
      response.status match {
        case Status.OK => (response.json \ "id").as[Long]
        case status => throw new ArtistsBackendException("failed to create artist")
      }
    }

  def editArtist(id: Long, artist: Artist) (implicit app: Application, ec: ExecutionContext) : Future[Boolean] =
    WS.url(s"$baseUrl/artists/$id/update").put(Json.toJson(artist)) map { response =>
      response.status match {
        case Status.OK => true
        case Status.NOT_FOUND => throw new ArtistsNotFoundException(s"artist with id $id not found")
        case status => throw new ArtistsBackendException(s"failed to edit artist: unexpected status code ($status)")
      }
    }

  def deleteArtist(id: Long) (implicit app: Application, ec: ExecutionContext) : Future[Unit] =
    WS.url(s"$baseUrl/artists/$id/delete").delete() map { response =>
      response.status match {
        case Status.OK => Unit
        case status => throw new ArtistsNotFoundException(s"unexpected status code ($status)")
      }
    }
}

case class Artist(name: String, description: String, id: Long = 0)

object Artist {
  implicit val jsonReads: Reads[Artist] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "description").read[String] and
    (JsPath \ "id").read[Long]
  ) (Artist.apply _)

  implicit val jsonWrites: Writes[Artist] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "description").write[String] and
    (JsPath \ "id").write[Long]
  ) (unlift(Artist.unapply))
}

class ArtistsBackendException(msg: String) extends RuntimeException(msg)
class ArtistsNotFoundException(msg: String) extends ArtistsBackendException(msg)