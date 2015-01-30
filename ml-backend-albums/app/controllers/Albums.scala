package controllers

import data.DataProvider
import models.Album
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Albums extends Controller {
  def all() = Action.async { implicit request =>
      DataProvider.getAllAlbums().map { artists =>
        Ok(Json.prettyPrint(JsObject(Seq(
          "values" -> JsArray(
            for (artist <- artists) yield JsObject(Seq(
              "id" -> JsNumber(artist.id),
              "name" -> JsString(artist.name),
              "description" -> JsString(artist.description),
              "year" -> JsNumber(artist.year),
              "artist_id" -> JsNumber(artist.artistId)
            ))
          )
        ))))
      } recover {
        case e =>
          InternalServerError(JsObject(Seq(
            "error" -> JsString(e.getMessage)
          )))
      }
  }

  def id(id: Int) = Action.async { implicit request =>
      val albumsFuture = DataProvider.getAlbum(id)

      albumsFuture map {
        case Some(album) =>
          Ok(Json.prettyPrint(JsObject(Seq(
            "id" -> JsNumber(album.id),
            "name" -> JsString(album.name),
            "description" -> JsString(album.description),
            "year" -> JsNumber(album.year),
            "artist_id" -> JsNumber(album.artistId)
          ))))

        case None =>
          NotFound(JsObject(Seq(
            "error" -> JsString(s"no artist with id '$id'")
          )))
      }
  }

  def insert() = Action { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        try {
          val album = parseAlbum(json)
          DataProvider.insertAlbum(album)
          Ok(JsObject(Seq(
            "message" -> JsString("success")
          )))
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

  def ofArtist(artistId: Long) = Action.async { implicit request =>
    DataProvider.albumsOfArtist(artistId) map { albums =>
      Ok(Json.prettyPrint(JsObject(Seq(
        "values" -> JsArray(
          for (album <- albums) yield JsObject(Seq(
            "id" -> JsNumber(album.id),
            "name" -> JsString(album.name),
            "description" -> JsString(album.description),
            "year" -> JsNumber(album.year),
            "artist_id" -> JsNumber(album.artistId)
          ))
        )
      ))))
    } recover {
      case e =>
        InternalServerError(JsObject(Seq(
          "error" -> JsString(e.getMessage)
        )))
    }
  }

  private def parseAlbum(json: JsValue): Album = Album(
    name = (json \ "name").as[String],
    description = (json \ "description").as[String],
    year = (json \ "year").as[Int],
    artistId = (json \ "artist_id").as[Long]
  )
}
