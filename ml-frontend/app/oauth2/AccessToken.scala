package oauth2

import java.time.{Duration, LocalDateTime}

class AccessToken(val userId: String,
                  val value: String,
                  val expiresIn: LocalDateTime,
                  val tokenType: String) {
  def this(userId: String, value: String, duration: Duration, tokenType: String = "bearer") =
    this(userId, value, LocalDateTime.now() plus duration, tokenType)

  def isExpired = LocalDateTime.now().isAfter(expiresIn)
}
