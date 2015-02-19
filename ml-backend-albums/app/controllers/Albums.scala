package controllers

import play.api.libs.json._
import play.api.mvc._
import play.api.libs.functional.syntax._

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.util.Try

import data.DataProvider
import models.Album

object Albums extends Controller {
  val DefaultPageLen = 2

  implicit val albumsWrites : Writes[Album] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "description").write[String] and
    (JsPath \ "year").write[Int] and
    (JsPath \ "artist_id").write[Long] and
    (JsPath \ "id").write[Long]
  ) (unlift(Album.unapply))

  def all(page: Option[Int], pageLen: Option[Int]) = Action.async { implicit request =>
    val albumsFuture = page match {
      case Some(pageValue) => DataProvider.getAllAlbums((pageValue - 1) * pageLen.getOrElse(DefaultPageLen), pageLen.getOrElse(DefaultPageLen))
      case None => DataProvider.getAllAlbums()
    }

    albumsFuture map { albums =>
      val pageVal: Int  = page.getOrElse(1)
      val count: Int  = albums.length

      Ok(Json.prettyPrint(Json.obj(
        "page" -> pageVal,
        "page_len" -> count,
        "values" -> albums
      )))
    }// recover {
//      case e => InternalServerError(Json.obj("error" -> e.getMessage))
//    }
  }

  def id(id: Int) = Action.async { implicit request =>
    val albumsFuture = DataProvider.getAlbum(id)

    albumsFuture map {
      case Some(album) => Ok(Json.prettyPrint(Json.toJson(album)))
      case None => NotFound(Json.obj("error" -> JsString(s"no artist with id '$id'")))
    }
  }

  def insert() = Action { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        try {
          val album = parseAlbum(json)
          val newId = DataProvider.insertAlbum(album)
          Ok(Json.obj("message" -> JsString("success"), "id" -> newId))
        }
        catch {
          case e: Exception => InternalServerError(JsObject(Seq("error" -> JsString(e.getMessage))))
        }
      case None => BadRequest(JsObject(Seq("error" -> JsString("empty body"))))
    }
  }

  def update(id: Long) = Action { implicit request =>
    request.body.asJson match {

      case Some(json) => try {
        val album = parseAlbum(json).copy(id = id)
        DataProvider.updateAlbum(album)
        Ok(JsObject(Seq("message" -> JsString("success"))))
      } catch {
        case e: Exception => InternalServerError(JsObject(Seq("error" -> JsString(e.getMessage))))
      }

      case None => BadRequest(JsObject(Seq("error" -> JsString("empty body"))))
    }
  }

  def delete(id: Long) = Action {
    Try {
      DataProvider.delete(id)
      Ok(Json.obj("message" -> "success"))
    } getOrElse InternalServerError(Json.obj("error" -> s"failed to delete album with id $id"))
  }

  def ofArtist(artistId: Long) = Action.async { implicit request =>
    DataProvider.albumsOfArtist(artistId) map { albums =>
      Ok(Json.prettyPrint(Json.obj("values" -> albums)))
    } recover {
      case e => InternalServerError(Json.obj("error" -> JsString(e.getMessage)))
    }
  }

  private def parseAlbum(json: JsValue): Album = Album(
    name = (json \ "name").as[String],
    description = (json \ "description").as[String],
    year = (json \ "year").as[Int],
    artistId = (json \ "artist_id").as[Long]
  )
}
