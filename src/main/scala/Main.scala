import scala.io.Source

/**
  * Created by Ryo Ota on 2016/11/07.
  */
object Main {

  /**
    * A sample of [[EngDocument]]s
    * @return
    */
  private[this] def sampleDocs(): Seq[EngDocument] = {
  // All documents (Hard Coding)
      val engDocuments: Seq[EngDocument] = Seq(
        EngDocument("weather is fine rainy."),
        EngDocument("weather cloudy fine."),
        EngDocument("basketball baseball soccer baseball")//,
  //      EngDocument("a soccer play is hold on Sunday"),
  //      EngDocument("sunny day is great for baseball games")
      )
    engDocuments
  }


  /**
    * A [[EngDocument]]s from the file
    * @return
    */
  private[this] def docsFromFile(): Seq[EngDocument] = {
    val filePathStr = "./data/documents.txt"
    Source.fromFile(filePathStr).getLines().map(EngDocument).toSeq
  }

  def main(args: Array[String]) {
//
//    // make train-set file and test-set file for SVM light
//    TrainAndTestFilesGenerator.generateSvmLightFormatFiles(
//      FigureAndSumoDataset,
//      trainFilePath = "./data/train-set",
//      testFilePath  = "./data/test-set",
//      trainSetRate = 0.8)


//    TrainAndTestFilesGenerator.generateMultiSvmLightFormatFiles(
//      JapanTimesDataset,
//      trainFilePath = "./data/multi-class-train-set",
//      testFilePath  = "./data/multi-class-test-set",
//      trainSetRate = 0.8
//    )

//    val MultiDataset(seq) = JapanTimesDonwloader.onlyTitleMultiClassifiable.multiDataset()
//    val lengths = seq.flatMap(_.map(_.entity.length))
//    val average = lengths.sum.toDouble / lengths.length
//    println("Average length of titles " + average) // 55.72


    // Train-Set: only title, Test-Set: only title
//    TrainAndTestFilesGenerator.generateMultiSvmLightFormatFiles(
//      JapanTimesDonwloader.onlyTitleMultiClassifiable,
//      trainFilePath = "./data/multi-class-only-title-train-set",
//      testFilePath  = "./data/multi-class-only-title-test-set",
//      trainSetRate = 0.8
//    )



    // Train-Set: title+article, Test-Set: only title
//    TrainAndTestFilesGenerator.generateTrainTestMultiSvmLightFormatFiles(
//      JapanTimesDonwloader.`Multi-Classfiable of (train-set: title + article, test-set: only title)`,
//      trainFilePath = "./data/multi-class-title-article-train-set",
//      testFilePath  = "./data/multi-class-title-article-test-set",
//      trainSetRate = 0.8
//    )


//    // Train-Set: many titles+article, Test-Set: only title
//    TrainAndTestFilesGenerator.generateTrainTestMultiSvmLightFormatFiles(
//      JapanTimesDonwloader.`Multi-Classfiable of (train-set: many titles + article, test-set: only title)`(titleTimes = 20),
//      trainFilePath = "./data/multi-class-20-titles-article-train-set",
//      testFilePath  = "./data/multi-class-20-titles-article-test-set",
//      trainSetRate = 0.8
//    )


//    TextGeneratorForGensim.generate(dirPath = "./gensim-text")


    // List titles for some articles
    val artsSeq: Seq[Seq[JapanTimesArticle]] = JapanTimesDonwloader.getJapanTimesArticlesSeq()
    artsSeq.flatMap(_.take(10)).map(_.title).foreach(println)

  }
}
