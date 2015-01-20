package data

import models.Artist

import scala.concurrent.ExecutionContext.Implicits.global

object TestData {
  def fill(): Unit = {

    DataProvider.getArtist(1) map {
      case None =>
        DataProvider.insertArtist(new Artist(
          name = "Foo Fighters",
          description = """Foo Fighters — американская альтернативная рок-группа, образованная бывшим участником рок-группы Nirvana Дейвом Гролом в 1995 году.""",
          id = 1
        ))

      case Some(_) =>
    }
  }
}
