import java.io.PrintWriter

import scala.io.Source

import java.io.{File, FileOutputStream, PrintWriter}

import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxProfile}

import scala.io.Source
//import scala.util.Marshal
import scala.pickling._
import json._
import scala.pickling.Defaults._
import scala.pickling.json._


/**
  * Created by Ryo on 2016/11/25.
  */
object FigureAndSumoDataset extends BinaryClassifiable{

  val japanTimesJsonFilePath = "./data/japan-times-docs.json"

  /**
    *
    * Figure URLs and Sumo URLs taken from the Web
    *
    * @return (figureUrls, sumoUrls)
    */
  private[this] def onlineJTDocsPair(): (List[EngDocument], List[EngDocument]) = {
    val pageLimit = 30
    val figureUrls = TimesGetterJsoup.getUrls(TimesGetterJsoup.figurePage, pageLimit)
    val sumoUrls   = TimesGetterJsoup.getUrls(TimesGetterJsoup.sumoPage, pageLimit)
    println("figure" + figureUrls)
    println("sumo  " + sumoUrls)

    val figureArt = figureUrls.flatMap(url => TimesGetterJsoup.getArticle(url)).map(EngDocument)
    val sumoArt   = sumoUrls.flatMap(url => TimesGetterJsoup.getArticle(url)).map(EngDocument)
    (figureArt, sumoArt)
  }


  /**
    * Figure URLs and Sumo URLs take from the stored file
    *
    * @return (figureUrls, sumoUrls)
    */
  private[this] lazy val offlineJTDocsPair: (List[EngDocument], List[EngDocument]) = {
    Source.fromFile(japanTimesJsonFilePath).mkString.unpickle[(List[EngDocument], List[EngDocument])]
  }

  /**
    * Donwload Japan Times documents into a file
    */
  private def downloadJapanTimesDocs(): Unit = {
    new PrintWriter(japanTimesJsonFilePath) {
      write(onlineJTDocsPair().pickle.value)
      close()
    }
  }

  override def binaryDataset(): BinaryDataset = {
    val (figureDocs, sumoDocs) = offlineJTDocsPair
    BinaryDataset(figureDocs, sumoDocs)
  }
}
