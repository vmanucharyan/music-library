package data

import models.Song

import scala.concurrent.ExecutionContext.Implicits.global

object TestData {
  def fill(): Unit = {

    DataProvider.getSong(1) map {
      case None =>
        val ffId = 1
        val wlId = 1

        DataProvider.insertSong(new Song(
          name = "Rope",
          genre = "Rock",
          durationSec = 259,
          albumId = wlId,
          artistId = ffId
        ))

        DataProvider.insertSong(new Song(
          name = "Bridge Burning",
          genre = "Rock",
          durationSec = 284,
          albumId = wlId,
          artistId = ffId
        ))

        DataProvider.insertSong(new Song(
          name = "Back & Forth",
          genre = "Rock",
          durationSec = 234,
          albumId = wlId,
          artistId = ffId
        ))

        DataProvider.insertSong(new Song(
          name = "Walk",
          genre = "Rock",
          durationSec = 256,
          albumId = wlId,
          artistId = ffId
        ))

        val echoesId = 2

        DataProvider.insertSong(new Song(
          name = "The Pretender",
          genre = "Rock",
          durationSec = 238,
          albumId = echoesId,
          artistId = ffId
        ))

        DataProvider.insertSong(new Song(
          name = "Long Road to Ruin",
          genre = "Rock",
          durationSec = 211,
          albumId = echoesId,
          artistId = ffId
        ))

        DataProvider.insertSong(new Song(
          name = "Let It Die",
          genre = "Rock",
          durationSec = 264,
          albumId = echoesId,
          artistId = ffId
        ))

      case Some(_) =>
    }
  }
}
