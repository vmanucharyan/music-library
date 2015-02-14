package controllers

import data.DataProvider
import models.Song
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.functional.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Songs extends Controller {
  val DefaultPageLen = 2

  implicit val songsWrites: Writes[Song] = (
    ( JsPath  \ "name").write[String] and
    ( JsPath  \ "genre").write[String] and
    ( JsPath  \ "duration_sec").write[Int] and
    ( JsPath  \ "album_id").write[Long] and
    ( JsPath  \ "artist_id").write[Long] and
    ( JsPath  \ "id").write[Long]
  ) (unlift(Song.unapply))

  implicit val songReads : Reads[Song] = (
    ( JsPath  \ "name").read[String] and
    ( JsPath  \ "genre").read[String] and
    ( JsPath  \ "duration_sec").read[Int] and
    ( JsPath  \ "album_id").read[Long] and
    ( JsPath  \ "artist_id").read[Long] and
    ( JsPath  \ "id").read[Long]
  ) (Song.apply _)

  def all(page: Option[Int], pageLen: Option[Int]) = Action.async {
    val songsFuture = page match {
      case Some(pageValue) => DataProvider.getAllSongs((pageValue - 1) * pageLen.getOrElse(DefaultPageLen), pageLen.getOrElse(DefaultPageLen))
      case None => DataProvider.getAllSongs()
    }

    songsFuture map { songs =>
      val currPage: Int = page.getOrElse(1)
      val count = songs.length

      Ok(Json.prettyPrint(Json.obj(
        "page" -> currPage,
        "page_len" -> count,
        "values" -> songs
      )))
    }
  }

  def id(id: Int) = Action.async { implicit request =>
    DataProvider.getSong(id).map {
      case Some(s) =>Ok(Json.toJson(s))
      case None => NotFound(Json.obj("error" -> s"no song with id '$id'"))
    } recover {
      case ex => InternalServerError(Json.obj("error" -> ex.getMessage))
    }
  }

  def ofAlbum(albumId: Long) = Action.async {
    DataProvider.songsOfAlbum(albumId) map { songs =>
      Ok(Json.prettyPrint(Json.obj("values" -> songs)))
    }
  }

  def ofArtist(artistId: Long) = Action.async {
    DataProvider.songsOfArtist(artistId) map { songs =>
      Ok(Json.prettyPrint(Json.obj("values" -> songs)))
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
