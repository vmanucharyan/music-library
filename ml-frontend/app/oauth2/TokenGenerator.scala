package oauth2

import scala.util.Random

trait TokenGenerator {
  def generateToken(): String
}

class AlphaNumericTokenGenerator(val length: Int  = 50) extends TokenGenerator {
  def generateToken(): String =
    String.join("", (Random.alphanumeric take length) toArray)
}
