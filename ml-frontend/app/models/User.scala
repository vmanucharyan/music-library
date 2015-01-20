package models

import java.security.MessageDigest

import play.api.db.slick.Config.driver.simple._

import scala.slick.ast.ColumnOption.Length

case class User (
  email: String,
  fullName: String,
  passHash: String
)

class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {
  def email = column[String]("email", O.PrimaryKey)
  def fullName = column[String]("full_name", O.NotNull)
  def passHash = column[String]("pass_hash", Length(256, varying = false))

  def * = (email, fullName, passHash) <> (User.tupled, User.unapply)
}

object UsersHelper {
  def hashPassword(pass: String) = {
    val hasher = MessageDigest.getInstance("SHA-256")
    new String(hasher.digest(pass.getBytes))
  }
}
