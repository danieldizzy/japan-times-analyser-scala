import java.io.{File, PrintWriter}

import scala.io.Source
//import scala.util.Marshal
import scala.pickling.Defaults._
import scala.pickling.json._


/**
  * Created by Ryo on 2016/11/25.
  */
object JapanTimesDataset extends MultiClassifiable{

  private val japanTimesJsonFilePath = "./data/japan-times-multi-docs.json"

//  /**
//    *
//    * Figure URLs and Sumo URLs taken from the Web
//    *
//    * @return (figureUrls, sumoUrls)
//    */
//  private[this] def onlineJTDocsPair(): (List[EngDocument], List[EngDocument]) = {
//    val pageLimit = 30
//    val figureUrls = TimesGetterJsoup.getUrls(TimesGetterJsoup.figurePage, pageLimit)
//    val sumoUrls   = TimesGetterJsoup.getUrls(TimesGetterJsoup.sumoPage, pageLimit)
//    println("figure" + figureUrls)
//    println("sumo  " + sumoUrls)
//
//    val figureArt = figureUrls.flatMap(url => TimesGetterJsoup.getArticle(url)).map(EngDocument)
//    val sumoArt   = sumoUrls.flatMap(url => TimesGetterJsoup.getArticle(url)).map(EngDocument)
//    (figureArt, sumoArt)
//  }


//  /**
//    * Figure URLs and Sumo URLs take from the stored file
//    *
//    * @return (figureUrls, sumoUrls)
//    */
//  private[this] lazy val offlineJTDocsPair: (List[EngDocument], List[EngDocument]) = {
//    Source.fromFile(japanTimesJsonFilePath).mkString.unpickle[(List[EngDocument], List[EngDocument])]
//  }
//
//  /**
//    * Donwload Japan Times documents into a file
//    */
//  private def downloadJapanTimesDocs(): Unit = {
//    new PrintWriter(japanTimesJsonFilePath) {
//      write(onlineJTDocsPair().pickle.value)
//      close()
//    }
//  }

  private def download(): Seq[Seq[EngDocument]] = {
    println("Downloading...")
    val pageLimit = 30
    val pageToUrls = Seq(
      TimesGetterJsoup.economyPage _,
      TimesGetterJsoup.politicPage _,
      TimesGetterJsoup.techPage _,
      TimesGetterJsoup.figurePage _,
      TimesGetterJsoup.sumoPage _
    )


    val docsSeq: Seq[Seq[EngDocument]] = pageToUrls.map{ pageToUrl =>
      TimesGetterJsoup
        .getUrls(pageToUrl, pageLimit)
        .flatMap { url =>
          TimesGetterJsoup.getArticle(url).map(EngDocument)
        }
    }
    println("Donwloaded!")

    docsSeq
  }

  override def multiDataset(): MultiDataset = {
    val docsSeq: Seq[Seq[EngDocument]] =
      // if the cache file exists, use cache file for MultiDataset
      if(new File(japanTimesJsonFilePath).exists()){
        Source.fromFile(japanTimesJsonFilePath).mkString.unpickle[Seq[Seq[EngDocument]]]
      } else {
        val docsSeq: Seq[Seq[EngDocument]]  = download()
        new PrintWriter(japanTimesJsonFilePath) {
          write(docsSeq.pickle.value)
          close()
        }
        docsSeq
      }
    MultiDataset(docsSeq)
  }
}
