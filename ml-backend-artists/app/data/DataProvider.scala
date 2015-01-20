package data

import models._
import play.api.Play.current
import play.api.db.slick._

import scala.concurrent.{ExecutionContext, Future}
import scala.slick.driver.H2Driver.simple._

object DataProvider {
  private val artists = TableQuery[ArtistsTable]

  // Artists

  def getAllArtists() (implicit context: ExecutionContext) : Future[List[Artist]] = Future {
    DB.withSession(implicit s => {
      artists.list
    })
  }

  def getArtist(id: Long) (implicit context: ExecutionContext) : Future[Option[Artist]] = Future {
    DB.withSession(implicit s => {
      artists.filter(e => e.id === id).firstOption
    })
  }

  def insertArtist(artist: Artist) : Unit = {
    DB.withSession(implicit s => {
      artists.insert(artist)
    })
  }
}
