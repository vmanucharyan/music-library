package oauth2

import scala.util.Random


trait AuthCodeGenerator {
  def generate(): String
}

class RandAuthCodeGenerator {
  def generate(): String =
    String.join("", (Random.alphanumeric take 10) toArray)
}
