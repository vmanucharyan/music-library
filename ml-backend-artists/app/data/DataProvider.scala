package data

import models._
import play.api.Play.current
import play.api.db.slick._

import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.Config.driver.simple._

object DataProvider {
  private val artists = TableQuery[ArtistsTable]

  // Artists

  def getAllArtists() (implicit context: ExecutionContext) : Future[List[Artist]] =
    DB.withSession(implicit s => Future {
      artists.list
    })

  def getArtist(id: Long) (implicit context: ExecutionContext) : Future[Option[Artist]] =
    DB.withSession(implicit s => Future {
      artists.filter(e => e.id === id).firstOption
    })

  def insertArtist(artist: Artist) : Long = {
    DB.withSession(implicit s => {
      (artists returning artists.map(a => a.id)).insert(artist)
    })
  }

  def updateArtist(artist: Artist) : Unit = DB.withSession(implicit s => {
    artists.filter(a => a.id === artist.id).update(artist)
  })

  def deleteArtist(id: Long) : Unit = DB.withSession(implicit s => {
    artists.filter(a => a.id === id).delete
  })
}
