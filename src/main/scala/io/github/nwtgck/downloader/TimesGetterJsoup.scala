package io.github.nwtgck.downloader

import io.github.nwtgck.datatype.JapanTimesArticle
import org.jsoup.Jsoup

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * Created by Jimmy on 25/01/2017.
  */
object TimesGetterJsoup {

  // page creator for "figure"
  def figurePage(pageNum: Int) = s"http://www.japantimes.co.jp/sports/figure-skating/figure-skating/page/${pageNum}/"
  // page creator for "somo"
  def sumoPage(pageNum: Int)   = s"http://www.japantimes.co.jp/sports/basho-reports/sumo/page/${pageNum}/"
  // page creator for politic
  def politicPage(pageNum: Int)   = s"http://www.japantimes.co.jp/news/politics-diplomacy/page/${pageNum}/"
  // page creator for politic
  def economyPage(pageNum: Int)   = s"http://www.japantimes.co.jp/news/economy-business/page/${pageNum}/"
  // page creator for tech
  def techPage(pageNum: Int)   = s"http://www.japantimes.co.jp/news/business/tech/page/${pageNum}/"


  /**
    * get URLs
    *
    * @param pageToUrl
    * @param pageLimit
    * @return
    */
  def getUrls(pageToUrl : (Int) => String, pageLimit: Int): List[String] = {
    var urls = List.empty[String]

    for(pageNum <- 1 to pageLimit) {

      val document: org.jsoup.nodes.Document = Jsoup.connect(pageToUrl(pageNum)).timeout(0).get()

      import scala.collection.JavaConversions._

      val artTags = document.getElementsByTag("article")
      for(artTag <- artTags.toStream){
        val aTag = artTag.getElementsByTag("a").first()
        val url  = aTag.attr("href")
        urls = urls :+ url
      }

    }

    urls
  }

  /**
    * get article
    *
    * @param url
    * @return
    */
  def getArticle(url: String): List[String] = {
    // Go to Japan Times

    println(s"Getting Article: ${url}")
    try{
      val doc = Jsoup.connect(url).timeout(0).get()
      List(doc.getElementById("jtarticle").text())
    } catch {
      case _: Throwable => List.empty
    }
  }

  def getJapanTimesArticleOpt(url: String): Option[JapanTimesArticle] = {
    // Go to Japan Times

    println(s"Getting Article: ${url}")
    Try{
      val doc = Jsoup.connect(url).timeout(0).get()
      JapanTimesArticle(
        url = url,
        title  = doc.select("#wrapper > div > div.main_content.content_styles > article > div.padding_block > header > hgroup > h1").get(0).text(),
        entity = doc.getElementById("jtarticle").text
      )
    }.toOption
  }

  def getJapanTimesArticleOptFuture(url: String)(implicit ec: ExecutionContext): Future[Option[JapanTimesArticle]] =
    Future(getJapanTimesArticleOpt(url))


  /**
    * get URLs Future
    *
    * @param pageToUrl
    * @param pageLimit
    * @return
    */
  def getUrlsFuture(pageToUrl : (Int) => String, pageLimit: Int)(implicit executionContext: ExecutionContext): Future[Seq[String]] = {
    var urls = List.empty[String]

    val futures: Seq[Future[Seq[String]]] = for(pageNum <- 1 to pageLimit)
      yield Future {
        val pageUrl = pageToUrl(pageNum)
        val document: org.jsoup.nodes.Document = {
          var tryDoc: Try[org.jsoup.nodes.Document] = null
          while({
            tryDoc = Try(Jsoup.connect(pageUrl).timeout(0).get())
            tryDoc}.isFailure
          ) {}
          tryDoc.get
        }
        val artTags = document.getElementsByTag("article")
        import scala.collection.JavaConversions._
        val urls = for {
            artTag <- artTags.toStream
              aTag = artTag.getElementsByTag("a").first()
              url = aTag.attr("href")
        } yield url
        println(s"Got article urls from ${pageUrl}")
        urls
      }

    Future.sequence(futures).map{_.flatten}
  }

}
