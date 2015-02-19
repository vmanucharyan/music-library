package models

import play.api.db.slick.Config.driver.simple._

case class Album (
  name: String,
  description: String,
  year: Int,
  artistId: Long,
  id: Long = 0
)

class AlbumsTable(tag: Tag) extends Table[Album](tag, "ALBUMS") {
  def name = column[String]("name")
  def description = column[String]("description")
  def year = column[Int]("year")
  def artistId = column[Long]("artist_id")
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
  
  def * = (name, description, year, artistId, id) <> (Album.tupled, Album.unapply)
}
