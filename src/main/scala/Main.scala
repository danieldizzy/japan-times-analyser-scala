import java.io.{File, PrintWriter}

import org.bson.types.BasicBSONList

import scala.io.Source
import scala.util.{Failure, Success}

/**
  * Created by Ryo Ota on 2016/11/07.
  */
object Main {

  /**
    * A sample of [[EngDocument]]s
    *
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
    *
    * @return
    */
  private[this] def docsFromFile(): Seq[EngDocument] = {
    val filePathStr = "./data/documents.txt"
    Source.fromFile(filePathStr).getLines().map(EngDocument).toSeq
  }

  def main(args: Array[String]) {

    if(false) {
      // make train-set file and test-set file for SVM light
      TrainAndTestFilesGenerator.generateSvmLightFormatFiles(
        FigureAndSumoDataset,
        trainFilePath = "./data/train-set",
        testFilePath = "./data/test-set",
        trainSetRate = 0.8
      )
    }


    if(false) {
      TrainAndTestFilesGenerator.generateMultiSvmLightFormatFiles(
        JapanTimesDataset,
        trainFilePath = "./data/multi-class-train-set",
        testFilePath = "./data/multi-class-test-set",
        trainSetRate = 0.8
      )
    }


    if(false) {
      // Avevate length of all titles
      val MultiDataset(seq) = JapanTimesDonwloader.onlyTitleMultiClassifiable.multiDataset()
      val lengths = seq.flatMap(_.map(_.entity.length))
      val average = lengths.sum.toDouble / lengths.length
      println("Average length of titles " + average) // 55.72
    }


    if(false) {
      // Train-Set: only title, Test-Set: only title
      TrainAndTestFilesGenerator.generateMultiSvmLightFormatFiles(
        JapanTimesDonwloader.onlyTitleMultiClassifiable,
        trainFilePath = "./data/multi-class-only-title-train-set",
        testFilePath = "./data/multi-class-only-title-test-set",
        trainSetRate = 0.8
      )
    }



    if(false) {
      // Train-Set: title+article, Test-Set: only title
      TrainAndTestFilesGenerator.generateTrainTestMultiSvmLightFormatFiles(
        JapanTimesDonwloader.`Multi-Classfiable of (train-set: title + article, test-set: only title)`,
        trainFilePath = "./data/multi-class-title-article-train-set",
        testFilePath = "./data/multi-class-title-article-test-set",
        trainSetRate = 0.8
      )
    }


    if(false){
      // Train-Set: many titles+article, Test-Set: only title
      TrainAndTestFilesGenerator.generateTrainTestMultiSvmLightFormatFiles(
        JapanTimesDonwloader.`Multi-Classfiable of (train-set: many titles + article, test-set: only title)`(titleTimes = 20),
        trainFilePath = "./data/multi-class-20-titles-article-train-set",
        testFilePath = "./data/multi-class-20-titles-article-test-set",
        trainSetRate = 0.8
      )
    }


    if(false) {
      // Generate article text from article data
      TextGeneratorForGensim.generateTextsSeparatedByGroups(dirPath = "./gensim-text")
    }



    if(false) {
      // List titles for some articles
      val artsSeq: Seq[Seq[JapanTimesArticle]] = JapanTimesDonwloader.getJapanTimesArticlesSeq()
      artsSeq.flatMap(_.take(10)).map(_.title).foreach(println)
    }


    if(false){
      // Generate one big article text from article data
      TextGeneratorForGensim.generateOneBigTextWithTitle(dirPath = "./gensim-text-for-word2vec")
    }


    if(false){
      val pycall = new PythonCall()
      pycall.send(funcName = "word2vec", "was") match {
        case Success(bsonList : BasicBSONList) =>

          // Convert bsonList to Array[Double]
          val featureVector: Array[Double] =
            (0 until bsonList.size())
              .map{idx => bsonList.get(idx).asInstanceOf[Double]}.toArray


          println(featureVector.toList)

        case Success(a) =>
          println(s"Unexpected value: ${a}")

        case Failure(exp) =>
          exp.printStackTrace()
      }
      pycall.close()
    }

    if(false){
      // make train-set file and test-set file (TFIDF & word2vec collaboration) for SVM light
      TrainAndTestFilesGenerator.generateMultiSvmLightFormatFilesWithGenerator(
        JapanTimesDataset,
        trainFilePath = "./data/multi-tfidf-word2vec-train-set",
        testFilePath  = "./data/multi-tfidf-word2vec-test-set",
        trainSetRate  = 0.8,
        generator     = FeatureVectorGeneratorE.`generate TFIDF & Word2Vec vectors and Words`
      )
    }


    if(false){
      // Generate one big article text from article data with title
      TextGeneratorForGensim.generateOneBigTextWithTitle(dirPath = "./gensim-text-for-word2vec")
    }


    if(false) {
      // Train-Set: many titles+article, Test-Set: only title
      TrainAndTestFilesGenerator.generateTrainTestMultiSvmLightFormatFilesWithGenerator(
        JapanTimesDonwloader.`Multi-Classfiable of (train-set: many titles + article, test-set: only title)`(titleTimes = 20),
        trainFilePath = "./data/multi-class-20-titles-tfidf-word2vec-article-train-set",
        testFilePath  = "./data/multi-class-20-titles-tfidf-word2vec-article-test-set",
        trainSetRate  = 0.8,
        generator     = FeatureVectorGeneratorE.`generate TFIDF & Word2Vec vectors and Words`

      )
    }

    if(true) {
      // Train-Set: many titles+article, Test-Set: only title
      TrainAndTestFilesGenerator.generateTrainTestMultiSvmLightFormatFilesWithGenerator(
        JapanTimesDonwloader.`Multi-Classfiable of (train-set: many titles + article, test-set: only title)`(titleTimes = 20),
        trainFilePath = "./data/multi-class-20-titles-tfidf-word2vec-tfidf-article-train-set",
        testFilePath  = "./data/multi-class-20-titles-tfidf-word2vec-tfidf-article-test-set",
        trainSetRate  = 0.8,
        generator     = FeatureVectorGeneratorE.`generate TFIDF vec ++ (Word2Vec*TFIDF) vectors and Words`

      )
    }





  }
}
