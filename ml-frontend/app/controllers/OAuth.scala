package controllers

import java.time.{Duration, LocalDateTime}

import data.DataProvider
import models.oauth2.OAuthApp
import oauth2._
import play.Logger
import play.api.Play.current
import play.api.cache.Cache
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object OAuth extends Controller {
  def auth(clientId: String, redirectUri: String, state: String) = Action.async { implicit rs =>
    if (AuthInfo.isAuthorized) {
      DataProvider.getApplication(clientId) map {
        case Some(app) =>
          Ok(views.html.oauth_grant_access(OAuthApp("id", "secret", "userId"), clientId, redirectUri, state))

        case None => Ok("error: wrong client_id")
      }

    }
    else {
      val returnUri = routes.OAuth.auth(clientId, redirectUri, state).absoluteURL()
      Future(Redirect(routes.SignIn.signIn(Some(returnUri))))
    }
  }

  def grantAccess(clientId: String, redirectUrl: String, state: String) = Action { implicit rs =>
    val codeGenerator = new RandAuthCodeGenerator()
    val code = codeGenerator.generate()

    Cache.set(code, clientId)
    
    Logger.debug(s"grant_access: code: $code")

    Redirect (
      url = redirectUrl,
      queryString = Map("code" -> Seq(code), "state" -> Seq(state))
    )
  }

  def token(code: String,
            clientId: String,
            clientSecret: String,
            redirectUri: String,
            grantType: String) = Action.async { implicit request =>

    Logger.debug(s"/token: code - $code")

    if (grantType != "bearer")
      Future(Redirect(redirectUri, Map("error" -> Seq("grant_type must be 'bearer'"))))

    Cache.getAs[String](code) match {
      case Some(cacheClientId) =>
        Logger.debug(s"$clientId $cacheClientId")

        if (clientId equals cacheClientId) {
          val tokenGenerator = new AlphaNumericTokenGenerator()
          val tokenString = tokenGenerator.generateToken()
          val token = new AccessToken(clientId, tokenString, Duration.ofMinutes(60))

          AuthSessionKeeper.storeToken(token)

          DataProvider.getApplication(clientId).map {
            case Some(app) =>
              if (app.id == token.userId && app.secret == clientSecret)
                Ok(JsObject(Seq(
                  "token" -> JsString(tokenString),
                  "expires_in" -> JsNumber(Duration.between(LocalDateTime.now(), token.expiresIn).getSeconds),
                  "grant_type" -> JsString("bearer")
                )))

              else
                Ok(JsObject(Seq(
                  "error" -> JsString("client_id does not match")
                )))

            case None =>
              Ok(JsObject(Seq(
                "grant_type" -> JsString("wrong client_id")
              )))
          }
        }
        else Future {
          Ok(JsObject(Seq(
            "grant_type" -> JsString("invalid code")
          )))
        }

      case None => Future {
        Ok(JsObject(Seq(
          "grant_type" -> JsString("invalid code0")
        )))
      }
    }
  }
}
