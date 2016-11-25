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

  val japanTimesJsonFilePath = "./data/japan-times-docs.json"

  private[this] def sampleDocs() = {
  // All documents (Hard Coding)
      val engDocuments: Seq[EngDocument] = Seq(
        EngDocument("weather is fine rainy."),
        EngDocument("weather cloudy fine."),
        EngDocument("basketball baseball soccer baseball")//,
  //      EngDocument("a soccer play is hold on Sunday"),
  //      EngDocument("sunny day is great for baseball games")
      )
  }

  private[this] def docsFromFile() = {
    val filePathStr = "./data/documents.txt"
    Source.fromFile(filePathStr).getLines().map(EngDocument(_)).toSeq
  }

  // return: structure (figureUrls, sumoUrls)
  private[this] def onlineJTDocsPair(): (List[EngDocument], List[EngDocument]) = {
//    lazy val dr = ??? //new FirefoxDriver()
    val pageLimit = 30
    val figureUrls = TimesGetterJsoup.getUrls(TimesGetterJsoup.figurePage, pageLimit)
    val sumoUrls   = TimesGetterJsoup.getUrls(TimesGetterJsoup.sumoPage, pageLimit)
    println("figure" + figureUrls)
    println("sumo  " + sumoUrls)

    val figureArt = figureUrls.flatMap(url => TimesGetterJsoup.getArticle(url)).map(EngDocument)
    val sumoArt   = sumoUrls.flatMap(url => TimesGetterJsoup.getArticle(url)).map(EngDocument)
    (figureArt, sumoArt)
  }

  private[this] lazy val offlineJTDocsPair: (List[EngDocument], List[EngDocument]) = {
    Source.fromFile(japanTimesJsonFilePath).mkString.unpickle[(List[EngDocument], List[EngDocument])]
  }

  private def downloadJapanTimesDocs(): Unit = {
    new PrintWriter(japanTimesJsonFilePath) {
      write(onlineJTDocsPair().pickle.value)
      close()
    }
  }

  def runFeatureCalc(): Unit = {

    val figureLabel    = "+1"
    val sumoLabel      = "-1"
    // number of training sets
    val trainSetNumber = 237
    // File names for train set and test set
    val trainFilePath  = "./data/train-set"
    val testFilePath   = "./data/test-set"
    val trainFileWriter      = new PrintWriter(new File(trainFilePath))
    val testFileWriter      =  new PrintWriter(new File(testFilePath))

    val (figureDocs, sumoDocs) = offlineJTDocsPair

    // All documents (from a file)
    val engDocuments: Seq[EngDocument] =
    //      docsFromFile()
      figureDocs ++ sumoDocs

    // Get the feature vectors and all words containing all documents
    val (featureVectors, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWords(engDocuments)

    println("--- All words ---")
//    println(allWords)
//    println(allWords.size)
    println(s"number of words: ${allWords.size}")

    println(figureDocs.length)
    println(sumoDocs.length)

    // make train file
    for(figureDoc <- figureDocs.take(trainSetNumber)){
      trainFileWriter.println(figureLabel + " " + featureVectors(figureDoc).zipWithIndex.filter{case (e, i) => e != 0}.toList.map{case (e, i) => s"${i+1}:${e}"}.mkString(" "))
    }

    for(sumoDoc <- sumoDocs.take(trainSetNumber)){
      trainFileWriter.println(sumoLabel + " " + featureVectors(sumoDoc).zipWithIndex.filter{case (e, i) => e != 0}.toList.map{case (e, i) => s"${i+1}:${e}"}.mkString(" "))
    }

    // make test file
    for(figureDoc <- figureDocs.drop(trainSetNumber)){
      testFileWriter.println(figureLabel + " " + featureVectors(figureDoc).zipWithIndex.filter{case (e, i) => e != 0}.toList.map{case (e, i) => s"${i+1}:${e}"}.mkString(" "))
    }

    for(sumoDoc <- sumoDocs.drop(trainSetNumber)){
      testFileWriter.println(sumoLabel + " " + featureVectors(sumoDoc).zipWithIndex.filter{case (e, i) => e != 0}.toList.map{case (e, i) => s"${i+1}:${e}"}.mkString(" "))
    }

    trainFileWriter.close()
    testFileWriter.close()



    System.exit(100)

    for((doc, featureVec) <- featureVectors) {
      println("--- Document ---")
      println(doc)
      println("--- Feature Vector ---")
      println(featureVec.map(e => "%.2f".format(e)).mkString("(", ", ", ")"))
    }

    println()

    println("--- Calculate similarities ---")
    //    val doc0 = engDocuments(0)
    //    val doc1 = engDocuments(1)
    //    val doc2 = engDocuments(2)
    //    println(s"0: ${doc0}")
    //    println(s"1: ${doc1}")
    //    println(s"2: ${doc2}")
    //    val doc00simu = Similarity.cosSimilarity(featureVectors(doc0), featureVectors(doc0))
    //    val doc01simu = Similarity.cosSimilarity(featureVectors(doc0), featureVectors(doc1))
    //    val doc02simu = Similarity.cosSimilarity(featureVectors(doc0), featureVectors(doc2))
    //    println(s"0 and 0: ${doc00simu}")
    //    println(s"0 and 1: ${doc01simu}")
    //    println(s"0 and 2: ${doc02simu}")

    for{
      doc1 <- engDocuments
      doc2 <- engDocuments
    } {
      println(s"doc1: ${doc1}")
      println(s"doc2: ${doc2}")
      println(s"similarity: ${ Similarity.cosSimilarity(featureVectors(doc1), featureVectors(doc2))}")
      println()
    }
  }

  def main(args: Array[String]) {
    println(offlineJTDocsPair._1.length)
    println(offlineJTDocsPair._2.length)

    println(offlineJTDocsPair._1.distinct.length)
    println(offlineJTDocsPair._2.distinct.length)

    runFeatureCalc()
//    downloadJapanTimesDocs()
//    println(offlineJTDocsPair())
  }
}
