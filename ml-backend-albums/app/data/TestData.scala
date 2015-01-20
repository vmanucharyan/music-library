package data

import models.Album

import scala.concurrent.ExecutionContext.Implicits.global

object TestData {
  def fill(): Unit = {

    DataProvider.getAlbum(1) map {
      case None =>
        DataProvider.insertAlbum(new Album(
          name = "Wasting Lights",
          year = 2010,
          description = """Wasting Light (в пер. с англ. Испепеляющий свет) — седьмой студийный альбом американской группы Foo Fighters, исполняющей альтернативный рок, выпущен звукозаписывающей компанией RCA Records в апреле 2011 года. Его название взято из текста песни «Miss the Misery». В записи пластинки приняли участие Боб Моулд и Крист Новоселич, а Пэт Смир снова официально значится участником коллектива — впервые после выпуска диска The Colour and the Shape (1997), хотя он играл на концертах Foo Fighters ещё с 2006 года.""",
          artistId = 1,
          id = 1
        ))

        DataProvider.insertAlbum(new Album(
          name = "Echoes, Silence, Patience & Grace",
          year = 2010,
          description = """Echoes, Silence, Patience & Grace — шестой студийный альбом американской рок-группы Foo Fighters, выпущенный 25 сентября 2007 года. Альбом продюсировал Гил Нортон, уже работавший с группой над её вторым альбомом, The Colour and the Shape.""",
          artistId = 1,
          id = 2
        ))

      case Some(_) =>
    }
  }
}
