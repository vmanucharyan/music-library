package controllers

import data.DataProvider
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
}
