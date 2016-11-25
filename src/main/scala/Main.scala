import java.io.{File, FileOutputStream, PrintWriter}

import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxProfile}

import scala.io.Source
//import scala.util.Marshal
import scala.pickling._
import json._
import scala.pickling.Defaults._
import scala.pickling.json._

/**
  * Created by Ryo Ota on 2016/11/07.
  */
object Main {

  /**
    * A sample of [[EngDocument]]s
    * @return
    */
  private[this] def sampleDocs(): Seq[EngDocument] = {
  // All documents (Hard Coding)
      val engDocuments: Seq[EngDocument] = Seq(
        EngDocument("weather is fine rainy."),
        EngDocument("weather cloudy fine."),
        EngDocument("basketball baseball soccer baseball")//,
  //      EngDocument("a soccer play is hold on Sunday"),
  //      EngDocument("sunny day is great for baseball games")
      )
    engDocuments
  }


  /**
    * A [[EngDocument]]s from the file
    * @return
    */
  private[this] def docsFromFile(): Seq[EngDocument] = {
    val filePathStr = "./data/documents.txt"
    Source.fromFile(filePathStr).getLines().map(EngDocument).toSeq
  }

  def main(args: Array[String]) {

    // make train-set file and test-set file for SVM light
    TrainAndTestFilesGenerator.generateSvmLightFormatFiles(
      FigureAndSumoDataset,
      trainFilePath = "./data/train-set",
      testFilePath  = "./data/test-set",
      trainSetRate = 0.8)

  }
}
