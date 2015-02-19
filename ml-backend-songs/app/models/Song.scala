package models

import play.api.db.slick.Config.driver.simple._

case class Song (
  name: String,
  genre: String,
  durationSec: Int,
  albumId: Long,
  artistId: Long,
  id: Long = 0
)

class SongsTable(tag: Tag) extends Table[Song](tag, "SONGS") {
  def name = column[String]("name")
  def genre = column[String]("genre")
  def durationSec = column[Int]("duration_sec")
  def albumId = column[Long]("album_id")
  def artistId = column[Long]("artist_id")
  def id = column[Long]("id", O.AutoInc, O.PrimaryKey)

  def * = (name, genre, durationSec, albumId, artistId, id) <> (Song.tupled, Song.unapply)
}
