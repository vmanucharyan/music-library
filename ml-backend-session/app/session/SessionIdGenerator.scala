package session

import scala.util.Random


trait SessionIdGenerator {
  def generate(): String
}

class RandomSessionIdGenerator(val len: Int = 20) extends SessionIdGenerator {
  def generate() : String = String.join("", Random.alphanumeric.take(len).toArray)
}
