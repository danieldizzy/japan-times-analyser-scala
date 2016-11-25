import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.By
import org.openqa.selenium.remote.RemoteWebDriver

import scala.collection.JavaConversions._

object TimesGetterJsoup {

  // page creator for "figure"

  def figurePage(pageNum: Int) = s"http://www.japantimes.co.jp/sports/figure-skating/figure-skating/page/${pageNum}/"
  // page creator for "somo"
  def sumoPage(pageNum: Int)   = s"http://www.japantimes.co.jp/sports/basho-reports/sumo/page/${pageNum}/"

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
      case e => List.empty
    }
  }

}
