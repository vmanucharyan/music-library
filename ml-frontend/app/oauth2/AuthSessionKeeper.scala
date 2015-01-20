package oauth2

import play.api.Play.current
import play.api.cache.Cache

object AuthSessionKeeper {
  def storeToken(token: AccessToken): Unit =
    Cache.set(token.value, token)

  def retreiveToken(tokenValue: String): Option[AccessToken] =
    Cache.getAs[AccessToken](tokenValue)

  def removeToken(token: AccessToken): Unit =
    Cache.remove(token.value)

  def storeOAuthCode(code: String, clientId: String): Unit =
    Cache.set(code, clientId)

  def retreiveOAuthCode(code: String): Option[String] =
    Cache.getAs[String](code)
}
