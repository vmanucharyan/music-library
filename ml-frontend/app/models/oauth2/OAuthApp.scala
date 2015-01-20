package models.oauth2

import play.api.db.slick.Config.driver.simple._

import models.UsersTable


case class OAuthApp(id: String, secret: String, userId: String)

class OAuthAppsTable(tag: Tag) extends Table[OAuthApp](tag, "OAUTH_APPS") {
  def id = column[String]("id", O.PrimaryKey)
  def secret = column[String]("secret", O.NotNull)
  def userId = column[String]("user_id", O.NotNull)

  def user = foreignKey("fk_user_id", userId, TableQuery[UsersTable])(u => u.email)

  def * = (id, secret, userId) <> (OAuthApp.tupled, OAuthApp.unapply)
}


case class RedirectUrl(id: Long, appId: String, url: String)

class RedirectUrlTable(tag: Tag) extends Table[RedirectUrl](tag, "OAUTH_REDIRECT_URL") {
  def id = column[Long]("id", O.PrimaryKey)
  def appId = column[String]("app_id", O.NotNull)
  def url = column[String]("app_id")

  def app = foreignKey("fk_app_id", appId, TableQuery[OAuthAppsTable])(e => e.id)

  def * = (id, appId, url) <> (RedirectUrl.tupled, RedirectUrl.unapply)
}
