import data.TestData


object Global extends play.api.GlobalSettings {
  override def onStart(app: play.api.Application): Unit = {
    TestData.fill()
  }
}
