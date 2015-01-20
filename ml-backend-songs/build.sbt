name := "ml-backend-songs"

version := "1.0"

lazy val `ml-backend-songs` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "0.8.0"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  