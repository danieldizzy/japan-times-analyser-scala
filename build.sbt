name := "japan-times-feature-vector-scala"

version := "1.0"

scalaVersion := "2.11.8"

val seleniumVersion = "2.53.1"

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion,
  "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVersion,
  "org.scala-lang.modules" %% "scala-pickling" % "0.10.1"
)