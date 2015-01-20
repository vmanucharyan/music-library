package oauth2

import data.DataProvider
import play.api.Play.current
import play.api.cache.Cache
import play.api.mvc.{AnyContent, Request}

object AuthInfo {
  def isAuthorized(implicit rs: Request[AnyContent]): Boolean =
    getUserToken() match {
      case Some(tokenValue) => getAccessToken(tokenValue).isDefined
      case None => false
    }

  def acessToken (implicit rs: Request[AnyContent]): Option[AccessToken] =
    getUserToken() match {
      case Some(userTokenString) => getAccessToken(userTokenString)
      case None => None
    }

  def userId(implicit rs: Request[AnyContent]): Option[String] =
    acessToken match {
      case Some(token) =>
        if (token.tokenType == "password") Some(token.userId)
        else DataProvider.getApplicationSync(token.userId) match {
          case Some(app) => Some(app.userId)
          case None => None
        }

      case None => None
    }

  private def getUserToken() (implicit rs: Request[AnyContent]): Option[String] =
    checkSession() match {
      case Some(token) => Some(token)
      case None => checkHeader() match {
        case Some(headerToken) => Some(headerToken)
        case None => checkQuery()
      }
    }

  private def checkHeader() (implicit rs: Request[AnyContent]): Option[String] =
    rs.headers.get("Authorization")

  private def checkSession() (implicit rs: Request[AnyContent]): Option[String] =
    rs.session.get("token")

  private def checkQuery() (implicit rs: Request[AnyContent]): Option[String] =
    rs.queryString.get("token") match {
      case Some(sequence) => Some(sequence(0))
      case None => None
    }

  private def getAccessToken(userToken: String) (implicit rs: Request[AnyContent]): Option[AccessToken] =
    Cache.getAs[AccessToken](userToken)
}
