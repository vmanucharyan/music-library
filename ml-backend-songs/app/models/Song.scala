package models

import play.api.db.slick.Config.driver.simple._


case class Song (
  name: String,
  genre: String,
  durationSec: Int,
  albumId: Long,
  id: Long = 0
)

class SongsTable(tag: Tag) extends Table[Song](tag, "SONGS") {
  def name = column[String]("name")
  def genre = column[String]("genre")
  def durationSec = column[Int]("duration_sec")
  def albumId = column[Long]("album")
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def albumFk = foreignKey("album_fk", albumId, TableQuery[SongsTable])(a => a.id)

  def * = (name, genre, durationSec, albumId, id) <> (Song.tupled, Song.unapply)
}
