package data

import models._
import play.api.Play.current
import play.api.db.slick._

import scala.concurrent.{ExecutionContext, Future}
import scala.slick.driver.H2Driver.simple._

object DataProvider {
  private val albums = TableQuery[AlbumsTable]

  // Albums

  def getAllAlbums() (implicit context: ExecutionContext) : Future[List[Album]] = Future {
    DB.withSession(implicit s => {
      albums.list
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

  def insertAlbum(album: Album) (implicit context: ExecutionContext) : Unit = {
    DB.withSession(implicit s => Future {
      albums.insert(album)
    })
  }
}
