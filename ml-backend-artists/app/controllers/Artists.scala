package controllers

import data.DataProvider
import models.Artist
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

object Artists extends Controller {
  def all() = Action.async {
    DataProvider.getAllArtists() map { artists =>
      Ok(JsObject(Seq(
        "values" -> JsArray(
          for (artist <- artists) yield JsObject(Seq(
            "id" -> JsNumber(artist.id),
            "name" -> JsString(artist.name),
            "description" -> JsString(artist.description)
          ))
        )
      )))
    } recover {
      case e =>
        InternalServerError(JsObject(Seq(
          "error" -> JsString(e.getMessage)
        )))
    }
  }

  def id(id: Int) = Action.async {
    DataProvider.getArtist(id) map {
      case Some(artist) =>
        Ok(JsObject(Seq(
          "id" -> JsNumber(artist.id),
          "name" -> JsString(artist.name),
          "description" -> JsString(artist.description)
        )))

      case None =>
        NotFound(JsObject(Seq(
          "error" -> JsString(s"no artist with id '$id'")
        )))
    } recover {
      case e =>
        InternalServerError(JsObject(Seq(
          "error" -> JsString(e.getMessage)
        )))
    }
  }


  def insertArtist() = Action { implicit request =>
    request.body.asJson match {
      case Some(json) => try {
        val artist = parseArtist(json)
        val id = DataProvider.insertArtist(artist)
        Ok(JsObject(Seq(
          "message" -> JsString("success"),
          "id" -> JsNumber(id)
        )))
      } catch {
        case e: Exception => InternalServerError(JsObject(Seq("error" -> JsString(e.getMessage))))
      }
      case None => BadRequest(JsObject(Seq("error" -> JsString("empty body"))))
    }
  }
  def updateArtist(id: Long) = Action { implicit request =>
    request.body.asJson match {
      case Some(json) => try {
        val artist = parseArtist(json).copy(id = id)
        DataProvider.updateArtist(artist)
        Ok(JsObject(Seq("message" -> JsString("success"))))
      } catch {
        case e: Exception => InternalServerError(JsObject(Seq("error" -> JsString(e.getMessage))))
      }
      case None => BadRequest(JsObject(Seq("error" -> JsString("empty body"))))
    }
  }
  private def parseArtist(js: JsValue): Artist = Artist (
    name = (js \ "name").as[String],
    description = (js \ "description").as[String]
  )
}
