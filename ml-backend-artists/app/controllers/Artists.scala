package controllers

import data.DataProvider
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
}
