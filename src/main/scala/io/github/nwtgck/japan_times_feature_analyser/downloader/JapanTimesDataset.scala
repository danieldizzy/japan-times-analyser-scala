package io.github.nwtgck.japan_times_feature_analyser.downloader

import java.io.{File, PrintWriter}

import io.github.nwtgck.japan_times_feature_analyser.datatype.{EngDocument, MultiDataset}

import scala.io.Source
import scala.pickling.Defaults._
import scala.pickling.json._

/**
  * Created by Ryo on 2016/11/25.
  */
object JapanTimesDataset{

  private val japanTimesJsonFilePath = "./data/japan-times-multi-docs.json"


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

  def multiDataset(): MultiDataset = {
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
