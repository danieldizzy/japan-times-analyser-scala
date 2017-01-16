import java.io.{File, PrintWriter}

import scala.io.Source

/**
  * Created by Ryo on 2017/01/15.
  */
object TextGeneratorForGensim {

  /**
    * Generate each text file to the corresponding article
    * @param dirPath
    */
  def generate(dirPath: String): Unit = {
    val artssSeq: Seq[Seq[JapanTimesArticle]] = JapanTimesDonwloader.getJapanTimesArticlesSeq()
    val EachCategoryNum = 20

    new File(dirPath).mkdir()
    for((docs, groupIdx) <- artssSeq.zipWithIndex){
      val groupPath = s"${dirPath}/group${groupIdx+1}"
      new File(groupPath).mkdir()
      for((doc, docIdx) <- docs.zipWithIndex) {
        val JapanTimesArticle(url, title, entity) = doc
        val filePath = s"${groupPath}/${docIdx+1}.txt"
        val fileWriter = new PrintWriter(new File(filePath))
//        fileWriter.println(title)
//        fileWriter.println(url)
        fileWriter.println(entity.replaceAll("[\\s\\n]+", " "))
        fileWriter.close()
        println(s"'${filePath}' finished")
      }
    }
  }
}
