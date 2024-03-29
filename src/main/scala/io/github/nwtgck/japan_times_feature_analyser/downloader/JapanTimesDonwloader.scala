package io.github.nwtgck.japan_times_feature_analyser.downloader

import java.io.{File, PrintWriter}

import io.github.nwtgck.japan_times_feature_analyser.datatype._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.pickling.Defaults._
import scala.pickling.json._

/**
  * Created by Ryo on 2016/11/25.
  */
object JapanTimesDonwloader{

  @deprecated
  private val __globalJapanTimesJsonFilePath = "./data/japan-times-multi-articles.json"


  private val pageToUrls = Seq(
    TimesGetterJsoup.economyPage _,
    TimesGetterJsoup.politicPage _,
    TimesGetterJsoup.techPage _,
    TimesGetterJsoup.figurePage _,
    TimesGetterJsoup.sumoPage _
  )

  private def download(): Seq[Seq[JapanTimesArticle]] = {
    println("Downloading...")
    val pageLimit = 30


    val artsSeq: Seq[Seq[JapanTimesArticle]] = pageToUrls.map{ pageToUrl =>
      TimesGetterJsoup
        .getUrls(pageToUrl, pageLimit)
        .flatMap { url =>
          TimesGetterJsoup.getJapanTimesArticleOpt(url) match {
            case Some(art) => List(art)
            case None      => List.empty
          }
        }
    }
    println("Donwloaded!")

    artsSeq
  }

  private def optionsFlatten[T](opts: Seq[Option[T]]): Seq[T] =
    opts.flatMap{
      case Some(e) => List(e)
      case None    => List.empty
    }

  private def concurrentDowlonad(): Seq[Seq[JapanTimesArticle]] = {
    println("Downloading...")
    val pageLimit = 30
    val pageToUrls = Seq(
      TimesGetterJsoup.economyPage _,
      TimesGetterJsoup.politicPage _,
      TimesGetterJsoup.techPage _,
      TimesGetterJsoup.figurePage _,
      TimesGetterJsoup.sumoPage _
    )

    import scala.concurrent.ExecutionContext.Implicits.global

    val future: Future[Seq[Seq[JapanTimesArticle]]] = Future.traverse(pageToUrls){pageToUrl =>
      val future = for {
        urls: Seq[String]            <- TimesGetterJsoup.getUrlsFuture(pageToUrl, pageLimit)
        arts: Seq[JapanTimesArticle] <- Future.sequence(urls.map{url => TimesGetterJsoup.getJapanTimesArticleOptFuture(url)}).map(optionsFlatten)
      } yield arts
      future
    }

    val artsSeq: Seq[Seq[JapanTimesArticle]] = Await.result(future, Duration.Inf)

    println("Donwloaded!")

    artsSeq
  }

  private def concurrentDowlonadWithPageToUrls(pageLimit: Int, pageToUrls: Seq[Int => String]): Seq[Seq[JapanTimesArticle]] = {
    println("Downloading...")

    import scala.concurrent.ExecutionContext.Implicits.global

    val future: Future[Seq[Seq[JapanTimesArticle]]] = Future.traverse(pageToUrls){pageToUrl =>
      val future = for {
        urls: Seq[String]            <- TimesGetterJsoup.getUrlsFuture(pageToUrl, pageLimit)
        arts: Seq[JapanTimesArticle] <- Future.sequence(urls.map{url => TimesGetterJsoup.getJapanTimesArticleOptFuture(url)}).map(optionsFlatten)
      } yield arts
      future
    }

    val artsSeq: Seq[Seq[JapanTimesArticle]] = Await.result(future, Duration.Inf)

    println("Donwloaded!")

    artsSeq
  }

  /**
    * Download or use cache file
    * @return
    */
  def getJapanTimesArticlesSeqByPath(downloadInfo: DownloadInfo): Seq[Seq[JapanTimesArticle]] = {
    // if the cache file exists, use cache file for MultiDataset
    if(new File(downloadInfo.storedPath).exists()){
      Source.fromFile(downloadInfo.storedPath).mkString.unpickle[Seq[Seq[JapanTimesArticle]]]
    } else {
      val docsSeq: Seq[Seq[JapanTimesArticle]]  = concurrentDowlonadWithPageToUrls(pageLimit = downloadInfo.pageLimit, pageToUrls = downloadInfo.pageToUrls)//download()
      new PrintWriter(downloadInfo.storedPath) {
        write(docsSeq.pickle.value)
        close()
      }
      docsSeq
    }
  }

  /**
    * Download or use cache file
    * @return
    */

  @deprecated
  def getJapanTimesArticlesSeq(): Seq[Seq[JapanTimesArticle]] = {
    // if the cache file exists, use cache file for MultiDataset
      if(new File(__globalJapanTimesJsonFilePath).exists()){
        Source.fromFile(__globalJapanTimesJsonFilePath).mkString.unpickle[Seq[Seq[JapanTimesArticle]]]
      } else {
        val docsSeq: Seq[Seq[JapanTimesArticle]]  = concurrentDowlonad()//download()
        new PrintWriter(__globalJapanTimesJsonFilePath) {
          write(docsSeq.pickle.value)
          close()
        }
        docsSeq
      }
  }

  /**
    * Get LabeledJapanTimesArticles (label is Int (1~5)) to download or use cache file
    *
    * @return
    */
  def getLabeledJapanTimesArticlesByPath(downloadInfo: DownloadInfo): Seq[LabeledJapanTimesArticle[Int]] = {
    val articlesSeq: Seq[Seq[JapanTimesArticle]] = getJapanTimesArticlesSeqByPath(downloadInfo)

    for{
      (articles, index) <- articlesSeq.zipWithIndex
      label = index+1
      article           <- articles
    } yield LabeledJapanTimesArticle[Int](
      label = label,
      url = article.url,
      title = article.title,
      entity = article.entity
    )
  }

  /**
    * Get LabeledJapanTimesArticles (label is Int (1~5)) to download or use cache file
    *
    * @return
    */
  @deprecated
  def getLabeledJapanTimesArticles: Seq[LabeledJapanTimesArticle[Int]] = {
    val articlesSeq: Seq[Seq[JapanTimesArticle]] = getJapanTimesArticlesSeq()

    for{
      (articles, index) <- articlesSeq.zipWithIndex
      label = index+1
      article           <- articles
    } yield LabeledJapanTimesArticle[Int](
      label = label,
      url = article.url,
      title = article.title,
      entity = article.entity
    )
  }

  @deprecated
  lazy val onlyTitleMultiDataset: MultiDataset = {
      val artsSeq: Seq[Seq[JapanTimesArticle]] = getJapanTimesArticlesSeq()
      MultiDataset(docsSeq = artsSeq.map{arts => arts.map(art => EngDocument(art.title))})
    }

  @deprecated
  lazy val onlyArticleMultiDataset: MultiDataset = {
      val artsSeq: Seq[Seq[JapanTimesArticle]] = getJapanTimesArticlesSeq()
      MultiDataset(docsSeq = artsSeq.map{arts => arts.map(art => EngDocument(art.entity))})
    }


  @deprecated
  lazy val `Multi-Dataset of (train-set: title + article, test-set: only title)`: TrainTestMultiDataset = {
      val artsSeq: Seq[Seq[JapanTimesArticle]] = getJapanTimesArticlesSeq()
      TrainTestMultiDataset(artsSeq.map(arts => arts.map(art =>
        TrainTestDocument(
          trainDocument = EngDocument(art.title + " " + art.entity),
          testDocument =  EngDocument(art.title)
        )
      )))
    }



  @deprecated
  def `Multi-Dataset of (train-set: many titles + article, test-set: only title)`(titleTimes: Int): TrainTestMultiDataset = {
      val artsSeq: Seq[Seq[JapanTimesArticle]] = getJapanTimesArticlesSeq()
      TrainTestMultiDataset(artsSeq.map(arts => arts.map(art =>
        TrainTestDocument(
          trainDocument = EngDocument((art.title + " ")*titleTimes + " " + art.entity),
          testDocument =  EngDocument(art.title)
        )
      )))
    }


  def `Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(downloadInfo: DownloadInfo): LabeledMultiDataset[Int] =
      LabeledMultiDataset(docs = getLabeledJapanTimesArticlesByPath(downloadInfo).map{art => LabeledDocument(art.label, art.entity)}, classNum = pageToUrls.length)


  def `Labeled Multi-Dataset of (train-set: only title, test-set: only title)`(downloadInfo: DownloadInfo): LabeledMultiDataset[Int] =
      LabeledMultiDataset(docs = getLabeledJapanTimesArticlesByPath(downloadInfo).map{art => LabeledDocument(art.label, art.title)}, classNum = downloadInfo.pageToUrls.length)



  def `Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes: Int, downloadInfo: DownloadInfo): LabeledTrainTestMultiDataset[Int] =
      LabeledTrainTestMultiDataset(
        trainTestDocs =
          getLabeledJapanTimesArticlesByPath(downloadInfo).map{
            art => LabeledTrainTestDocument(art.label, EngDocument(art.entity + " " + ((art.title+" ")*titleTimes)), EngDocument((art.title + " ") * titleTimes))
          },
        classNum = downloadInfo.pageToUrls.length
      )



  def `Labeled Dataset of (train-set: (article + many titles), test-set: title)`(titleTimes: Int, downloadInfo: DownloadInfo): LabeledTrainTestMultiDataset[Int] =
      LabeledTrainTestMultiDataset(
        trainTestDocs =
          getLabeledJapanTimesArticlesByPath(downloadInfo).map{
            art => LabeledTrainTestDocument(art.label, EngDocument(art.entity + " " + ((art.title+" ")*titleTimes)), EngDocument(art.title))
          },
        classNum = downloadInfo.pageToUrls.length
      )




  //  override def multiDataset(): MultiDataset = {
//    val docsSeq: Seq[Seq[EngDocument]] =
//      // if the cache file exists, use cache file for MultiDataset
//      if(new File(japanTimesJsonFilePath).exists()){
//        Source.fromFile(japanTimesJsonFilePath).mkString.unpickle[Seq[Seq[EngDocument]]]
//      } else {
//        val docsSeq: Seq[Seq[EngDocument]]  = download()
//        new PrintWriter(japanTimesJsonFilePath) {
//          write(docsSeq.pickle.value)
//          close()
//        }
//        docsSeq
//      }
//    MultiDataset(docsSeq)
//  }
}
