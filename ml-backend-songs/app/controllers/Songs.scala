package controllers

import data.DataProvider
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

object Songs extends Controller {
  def all() = Action.async {
    DataProvider.getAllSongs().map { songs =>
      Ok(JsObject(Seq(
        "values" -> JsArray(
          for (song <- songs) yield JsObject(Seq(
            "id" -> JsNumber(song.id),
            "name" -> JsString(song.name),
            "genre" -> JsString(song.genre),
            "duration_sec" -> JsNumber(song.durationSec),
            "album_id" -> JsNumber(song.albumId)
          ))
        )
      )))
    }
  }

  def id(id: Int) = Action.async { implicit request =>
    DataProvider.getSong(id).map {
      case Some(s) =>
        Ok(JsObject(Seq(
          "id" -> JsNumber(s.id),
          "name" -> JsString(s.name),
          "genre" -> JsString(s.genre),
          "duration_sec" -> JsNumber(s.durationSec),
          "album_id" -> JsNumber(s.albumId)
        )))

      case None =>
        NotFound(JsObject(Seq(
          "error" -> JsString(s"no song with id '$id'")
        )))

    } recover {
      case ex =>
        InternalServerError(JsObject(Seq(
          "error" -> JsString(ex.getMessage)
        )))
    }
  }
}
