package controllers

import data.DataProvider
import models.Song
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Songs extends Controller {
  def all() = Action.async {
    DataProvider.getAllSongs().map { songs =>
      Ok(Json.prettyPrint(Json.obj(
        "values" -> JsArray(
          for (song <- songs) yield Json.obj(
            "id" -> JsNumber(song.id),
            "name" -> JsString(song.name),
            "genre" -> JsString(song.genre),
            "duration_sec" -> JsNumber(song.durationSec),
            "album_id" -> JsNumber(song.albumId)
          )
        )
      )))
    }
  }

  def id(id: Int) = Action.async { implicit request =>
    DataProvider.getSong(id).map {
      case Some(s) =>
        Ok(Json.prettyPrint(Json.obj(
          "id" -> JsNumber(s.id),
          "name" -> JsString(s.name),
          "genre" -> JsString(s.genre),
          "duration_sec" -> JsNumber(s.durationSec),
          "album_id" -> JsNumber(s.albumId)
        )))

      case None =>
        NotFound(Json.obj("error" -> s"no song with id '$id'"))

    } recover {
      case ex => InternalServerError(Json.obj("error" -> ex.getMessage))
    }
  }

  def ofAlbum(albumId: Long) = Action.async {
    DataProvider.songsOfAlbum(albumId) map { songs =>
      Ok(Json.prettyPrint(Json.obj(
        "values" -> Json.arr(
          for (song <- songs) yield Json.obj(
            "id" -> JsNumber(song.id),
            "name" -> JsString(song.name),
            "genre" -> JsString(song.genre),
            "duration_sec" -> JsNumber(song.durationSec),
            "album_id" -> JsNumber(song.albumId)
          )
        )
      )))
    }
  }

  def ofArtist(artistId: Long) = Action.async {
    DataProvider.songsOfArtist(artistId) map { songs =>
      Ok(Json.prettyPrint(Json.obj(
        "values" -> Json.arr(
          for (song <- songs) yield Json.obj(
            "id" -> JsNumber(song.id),
            "name" -> JsString(song.name),
            "genre" -> JsString(song.genre),
            "duration_sec" -> JsNumber(song.durationSec),
            "album_id" -> JsNumber(song.albumId)
          )
        )
      )))
    }
  }

  def insertSong() = Action { implicit request =>
    request.body.asJson match {
      case Some(json) => try {

        val song = parseSong(json)
        val id = DataProvider.insertSong(song)

        Ok(Json.obj("message" -> JsString("success"), "id" -> JsNumber(id)))

      } catch {
        case e: Exception => InternalServerError(JsObject(Seq("error" -> JsString(e.getMessage))))
      }

      case None => BadRequest(Json.obj("error" -> JsString("empty body")))
    }
  }

  def update(id: Long) = Action.async { implicit request =>
    request.body.asJson match {
      case Some(json) => DataProvider.getSong(id) map {

        case Some(song) =>
          val song = parseSong(json).copy(id = id)
          DataProvider.updateSong(song)
          Ok(JsObject(Seq("message" -> JsString("success"))));

        case None => NotFound(Json.obj("message" -> JsString(s"song with id='$id' not found")))

      }

      case None => Future(BadRequest(Json.obj("error" -> JsString("empty body"))))
    }
  }

  def delete(id: Long) = Action { implicit request =>
    DataProvider.deleteSong(id)
    Ok(Json.obj("message" -> "success"))
  }

  private def parseSong(json: JsValue): Song = Song(
    name = (json \ "name").as[String],
    genre = (json \ "genre").as[String],
    durationSec = (json \ "duration_sec").as[Int],
    albumId = (json \ "album_id").as[Long],
    artistId = (json \ "artist_id").as[Long]
  )
}
