package models

import play.api.db.slick.Config.driver.simple._


case class Artist (
  name: String,
  description: String,
  id: Long = 0
)

class ArtistsTable(tag: Tag) extends Table[Artist](tag, "ARTISTS") {
  def name = column[String]("name")
  def description = column[String]("description")
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def * = (name, description, id) <> (Artist.tupled, Artist.unapply)
}
