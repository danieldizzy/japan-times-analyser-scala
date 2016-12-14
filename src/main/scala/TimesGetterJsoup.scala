import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.collection.JavaConversions._
import scala.util.Try

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
    * @param pageToUrl
    * @param pageLimit
    * @return
    */
  def getUrls(pageToUrl : (Int) => String, pageLimit: Int): List[String] = {
    var urls = List.empty[String]

    for(pageNum <- 1 to pageLimit) {

      val document: Document = Jsoup.connect(pageToUrl(pageNum)).timeout(0).get()

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

}
