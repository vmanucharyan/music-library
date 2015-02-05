package data

import models.{Song, SongsTable}
import play.api.Play.current
import play.api.db.slick._

import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.Config.driver.simple._

object DataProvider {
  private val songs = TableQuery[SongsTable]

  // Songs

  def getAllSongs() (implicit context: ExecutionContext) : Future[List[Song]] = Future {
    DB.withSession(implicit s => {
      songs.list
    })
  }

  def getSong(id: Long) (implicit context: ExecutionContext) : Future[Option[Song]] = Future {
    DB.withSession(implicit s => {
      songs.filter(e => e.id === id).firstOption
    })
  }
  
  def findSongByName(name: String) (implicit contxt: ExecutionContext) : Future[List[Song]] = Future {
    DB.withSession(implicit s => {
      songs.filter(e => e.name === name).list
    })
  }

  def songsOfAlbum(albumId: Long) (implicit context: ExecutionContext) : Future[List[Song]] = Future {
    DB.withSession(implicit s => {
      songs.filter(e => e.albumId === albumId).list
    })
  }

  def songsOfArtist(artistId: Long) (implicit context: ExecutionContext) : Future[List[Song]] =
    DB.withSession(implicit s => Future {
      songs.filter(s => s.artistId === artistId).list
    })

  def insertSong(song: Song) : Long = {
    DB.withSession(implicit s => {
      (songs returning songs.map(song => song.id)).insert(song)
    })
  }

  def updateSong(song: Song) : Unit = DB.withSession(implicit session => {
    songs.filter(s => s.id === song.id).update(song)
  })

  def deleteSong(id: Long) : Unit = DB.withSession(implicit session => {
    songs.filter(s => s.id === id).delete
  })
}
