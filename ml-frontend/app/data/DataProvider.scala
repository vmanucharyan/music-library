package data

import models._
import models.oauth2.{OAuthApp, OAuthAppsTable}
import play.api.Play.current
import play.api.db.slick._

import scala.concurrent.{ExecutionContext, Future}
import scala.slick.driver.H2Driver.simple._

object DataProvider {
  private val users = TableQuery[UsersTable]
  private val apps = TableQuery[OAuthAppsTable]

  // Users

  def getUserById(id: String)(implicit context: ExecutionContext): Future[Option[User]] = Future {
    DB.withSession(implicit session => {
      users.filter(u => u.email === id).firstOption
    })
  }

  def getUsers()(implicit context: ExecutionContext): Future[List[User]] = Future {
    DB.withSession(implicit s => {
      users.list
    })
  }

  def getApplication(id: String) (implicit context: ExecutionContext) : Future[Option[OAuthApp]] = Future {
    DB.withSession(implicit s => {
      apps.filter(a => a.id === id).firstOption
    })
  }

  def getApplicationSync(id: String): Option[OAuthApp] = {
    DB.withSession(implicit s => {
      apps.filter(a => a.id === id).firstOption
    })
  }

  def getUserApps(id: String) (implicit context: ExecutionContext) : Future[List[OAuthApp]] = Future {
    DB.withSession(implicit s => {
      apps.filter(app => app.userId === id).list
    })
  }

  def insertUser(user: User) (implicit context: ExecutionContext) : Future[Unit] = Future {
    DB.withSession(implicit s => {
      users.insert(user)
    })
  }

  def insertApplication(app: OAuthApp) (implicit context: ExecutionContext): Future[Unit] = Future {
    DB.withSession(implicit s => {
      apps.insert(app)
    })
  }
}
