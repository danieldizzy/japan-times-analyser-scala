import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.By
import collection.JavaConversions._

object TimesGetter {

  // page creator for "figure"
  def figurePage(pageNum: Int) = s"http://www.japantimes.co.jp/sports/figure-skating/figure-skating/page/${pageNum}/"
  // page creator for "somo"
  def sumoPage(pageNum: Int) = s"http://www.japantimes.co.jp/sports/basho-reports/sumo/page/${pageNum}/"

  /**
    * get URLs
    * @param dr
    * @param pageToUrl
    * @param pageLimit
    * @return
    */
  def getUrls(dr: RemoteWebDriver, pageToUrl : (Int) => String, pageLimit: Int): List[String] = {
    var urls = List.empty[String]

    for(pageNum <- 1 to pageLimit) {

      // Go to Japan Times
      dr.get(pageToUrl(pageNum))
//
//      // the number of article
//      val articleNumber = {
//        val sectionTag = dr.findElementByXPath("""//*[@id="wrapper"]/section/div[1]/div/section""")
//        val artTags = dr.findElementsByClassName("archive_story")
//        artTags.size()
//      }

      val articleTags = dr.findElementsByTagName("article")
      for(articleTag <- articleTags){
        val aTag = articleTag.findElement(By.cssSelector("a"))
        // get link
        val url = aTag.getAttribute("href")
        urls = urls :+ url
      }

//      println(s"article num: ${articleNumber}")

//      for (articleIdx <- 1 to articleNumber) {
//        val aTag = dr.findElementByXPath(s"""//*[@id="wrapper"]/section/div[1]/div/section/article[${articleIdx}]/div[1]/header/hgroup/h1/a""")
//        // get link
//        val url = aTag.getAttribute("href")
//        urls = urls :+ url
//      }

    }

    urls
  }

  /**
    * get article
    * @param dr
    * @param url
    * @return
    */
  def getArticle(dr: RemoteWebDriver, url: String): List[String] = {
    // Go to Japan Times
    dr.get(url)
    println(url)
    try{
      val jArtTag = dr.findElementByXPath(s"""//*[@id="jtarticle"]""")
      List(jArtTag.getText)
    } catch {
      case e => List.empty
    }
  }

}
