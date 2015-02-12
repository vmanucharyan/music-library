package session

import play.api.Application
import play.api.cache.Cache


class SessionKeeper {
  def storeSession(session: Session) (implicit app: Application) : Unit =
    session.id match {
      case Some(id) => Cache.set(id, session)
      case None => throw new IllegalArgumentException("session must have id")
    }

  def retreiveSession(id: String) (implicit app: Application) : Option[Session] =
    Cache.getAs[Session](id)
}
