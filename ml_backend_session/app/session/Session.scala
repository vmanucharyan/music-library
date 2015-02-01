package session


case class Session(userId: String, authToken: String, id: Option[String])
