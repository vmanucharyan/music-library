package oauth2

import scala.util.Random

trait AppCredsGenerator {
  def generateId(): String
  def generateSecret(): String
}

class RandomAppCredsGenerator(val idLen: Int = 10, val secretLen: Int = 20) {
  def generateId(): String = String.join("", Random.alphanumeric take idLen toArray)
  def generateKey(): String = String.join("", Random.alphanumeric take secretLen toArray)
}
