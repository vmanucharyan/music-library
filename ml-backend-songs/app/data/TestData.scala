package data

import models.Song

import scala.concurrent.ExecutionContext.Implicits.global

object TestData {
  def fill(): Unit = {

    DataProvider.getSong(1) map {
      case None =>
        DataProvider.insertSong(new Song(
          name = "Rope",
          genre = "Rock",
          durationSec = 259,
          albumId = 1
        ))

        DataProvider.insertSong(new Song(
          name = "Bridge Burning",
          genre = "Rock",
          durationSec = 284,
          albumId = 1
        ))

        DataProvider.insertSong(new Song(
          name = "Back & Forth",
          genre = "Rock",
          durationSec = 234,
          albumId = 1
        ))

        DataProvider.insertSong(new Song(
          name = "Walk",
          genre = "Rock",
          durationSec = 256,
          albumId = 1
        ))

      case Some(_) =>
    }
  }
}
