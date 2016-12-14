import java.io.{File, PrintWriter}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source
//import scala.util.Marshal
import scala.pickling.Defaults._
import scala.pickling.json._


/**
  * Created by Ryo on 2016/11/25.
  */
object JapanTimesDonwloader{

  private val japanTimesJsonFilePath = "./data/japan-times-multi-articles.json"


  private def download(): Seq[Seq[JapanTimesArticle]] = {
    println("Downloading...")
    val pageLimit = 30
    val pageToUrls = Seq(
      TimesGetterJsoup.economyPage _,
      TimesGetterJsoup.politicPage _,
      TimesGetterJsoup.techPage _,
      TimesGetterJsoup.figurePage _,
      TimesGetterJsoup.sumoPage _
    )


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

  /**
    * Download or use cache file
    * @return
    */
  private def getJapanTimesArticlesSeq(): Seq[Seq[JapanTimesArticle]] = {
    // if the cache file exists, use cache file for MultiDataset
      if(new File(japanTimesJsonFilePath).exists()){
        Source.fromFile(japanTimesJsonFilePath).mkString.unpickle[Seq[Seq[JapanTimesArticle]]]
      } else {
        val docsSeq: Seq[Seq[JapanTimesArticle]]  = concurrentDowlonad()//download()
        new PrintWriter(japanTimesJsonFilePath) {
          write(docsSeq.pickle.value)
          close()
        }
        docsSeq
      }
  }

  lazy val onlyTitleMultiClassifiable: MultiClassifiable = new MultiClassifiable {
    override def multiDataset(): MultiDataset = {
      val artsSeq: Seq[Seq[JapanTimesArticle]] = getJapanTimesArticlesSeq()
      MultiDataset(docsSeq = artsSeq.map{arts => arts.map(art => EngDocument(art.title))})
    }
  }

  lazy val onlyArticleMultiClassifiable: MultiClassifiable = new MultiClassifiable {
    override def multiDataset(): MultiDataset = {
      val artsSeq: Seq[Seq[JapanTimesArticle]] = getJapanTimesArticlesSeq()
      MultiDataset(docsSeq = artsSeq.map{arts => arts.map(art => EngDocument(art.entity))})
    }
  }



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