name := "japan-times-feature-vector-scala"

version := "1.0"

scalaVersion := "2.10.4"

val seleniumVersion = "2.53.1"

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion,
  "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVersion,

  "org.scala-lang.modules" %% "scala-pickling" % "0.10.1",
  "org.jsoup" % "jsoup" % "1.10.1",

  "org.mongodb" % "mongo-java-driver" % "3.4.1",

  // nak
  "org.scalanlp" % "nak" % "1.2.1"

)