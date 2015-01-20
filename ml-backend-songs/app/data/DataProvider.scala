package data

import models.{Song, SongsTable}
import play.api.Play.current
import play.api.db.slick._

import scala.concurrent.{ExecutionContext, Future}
import scala.slick.driver.H2Driver.simple._

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

  def insertSong(song: Song) (implicit context: ExecutionContext) : Unit = {
    DB.withSession(implicit s => {
      songs.insert(song)
    })
  }
}
