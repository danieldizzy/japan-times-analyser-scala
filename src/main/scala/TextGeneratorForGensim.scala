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
  def generateTextsSeparatedByGroups(dirPath: String): Unit = {
    val artsSeq: Seq[Seq[JapanTimesArticle]] = JapanTimesDonwloader.getJapanTimesArticlesSeq()

    new File(dirPath).mkdir()
    for((docs, groupIdx) <- artsSeq.zipWithIndex){
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


  /**
    * Generate a big text file including all article
    * @param dirPath
    */
  def generateOneBigText(dirPath: String): Unit = {
    val artsSeq: Seq[Seq[JapanTimesArticle]] = JapanTimesDonwloader.getJapanTimesArticlesSeq()

    new File(dirPath).mkdir()

    val filePath = s"${dirPath}/jp_times.txt"
    val fileWriter = new PrintWriter(new File(filePath))
    artsSeq.flatten.foreach{art =>
      fileWriter.println(art.entity)
    }
    fileWriter.close()
  }

  /**
    * Generate a big text file including all article with title
    * @param dirPath
    */
  def generateOneBigTextWithTitle(dirPath: String): Unit = {
    val artsSeq: Seq[Seq[JapanTimesArticle]] = JapanTimesDonwloader.getJapanTimesArticlesSeq()

    new File(dirPath).mkdir()

    val filePath = s"${dirPath}/jp_times_with_title.txt"
    val fileWriter = new PrintWriter(new File(filePath))
    artsSeq.flatten.foreach{art =>
      fileWriter.println(s"${art.title} ${art.entity}")
    }
    fileWriter.close()
  }
}
