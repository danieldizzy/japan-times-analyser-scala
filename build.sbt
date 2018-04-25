name := "japan-times-analyser"

version := "1.0"

scalaVersion := "2.11.12"

val seleniumVersion = "2.53.1"

javaOptions in run += "-Xms16384m"
javaOptions in run += "-Xmx16384m"

libraryDependencies ++= Seq(
  "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion,
  "org.seleniumhq.selenium" % "selenium-firefox-driver" % seleniumVersion,

  "org.scala-lang.modules" %% "scala-pickling" % "0.10.1",
  "org.jsoup" % "jsoup" % "1.10.1",

  "org.mongodb" % "mongo-java-driver" % "3.4.1",

  "org.apache.spark" %% "spark-mllib" % "2.1.0",
  "com.github.fommil.netlib" % "all" % "1.1.2"  // to improve performance

)
