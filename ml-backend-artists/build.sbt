name := "ml-backend-artists"

version := "1.0"

lazy val `ml-backend-artists` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq( jdbc , anorm , cache , ws )

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "0.8.0",
  "jp.t2v" %% "play2-auth" % "0.13.0"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )
