name := "word2vec"

version := "1.0"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-Xmax-classfile-name","78")

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-mllib" % "2.1.0",
  "com.github.fommil.netlib" % "all" % "1.1.2"  // to improve performance
)