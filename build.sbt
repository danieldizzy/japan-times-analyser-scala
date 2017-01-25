name := "japan-times-feature-vector-scala"

version := "1.0"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-Xmax-classfile-name","78")

lazy val root       = (project in file("."))
    .dependsOn(word2vec, downloader, datatype)

lazy val word2vec   = (project in file("word2vec"))

lazy val downloader = (project in file("downloader"))
    .dependsOn(datatype)

lazy val datatype   = (project in file("datatype"))

libraryDependencies ++= Seq(
  "org.mongodb" % "mongo-java-driver" % "3.4.1",

  // nak
  "org.scalanlp" % "nak_2.10" % "1.3"
)