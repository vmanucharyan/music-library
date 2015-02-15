package data

import models._
import play.api.Play.current
import play.api.db.slick._

import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.Config.driver.simple._

object DataProvider {
  private val albums = TableQuery[AlbumsTable]

  // Albums

  def getAllAlbums() (implicit context: ExecutionContext) : Future[List[Album]] = Future {
    DB.withSession(implicit s => {
      albums.list
    })
  }

  def getAllAlbums(first: Int, count: Int) (implicit ec: ExecutionContext) : Future[List[Album]] = Future {
    DB.withSession(implicit session => {
      albums.drop(first).take(count).list
    })
  }

  def getAlbum(id: Long) (implicit context: ExecutionContext) : Future[Option[Album]] = Future {
    DB.withSession(implicit s => {
      albums.filter(a => a.id === id).firstOption
    })
  }

  def findAlbumByName(name: String) (implicit context: ExecutionContext) : Future[List[Album]] = Future {
    DB.withSession(implicit s => {
      albums.filter(a => a.name === name).list
    })
  }

  def insertAlbum(album: Album) (implicit context: ExecutionContext) : Long = {
    DB.withSession(implicit s => {
      (albums returning albums.map(a => a.id)).insert(album)
    })
  }

  def updateAlbum(album: Album) : Unit = {
    DB.withSession(implicit s => {
      albums.filter(a => a.id === album.id).update(album)
    })
  }

  def albumsOfArtist(artistId: Long) (implicit context: ExecutionContext) : Future[List[Album]] = Future {
    DB.withSession(implicit s => {
      albums.filter(a => a.artistId === artistId).list
    })
  }

  def delete(id: Long) : Unit = DB.withSession(implicit session => {
    albums.filter(a => a.id === id).delete
  })
}
