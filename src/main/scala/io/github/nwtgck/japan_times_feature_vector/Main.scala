package io.github.nwtgck.japan_times_feature_vector

import io.github.nwtgck.japan_times_feature_vector.classifier.LogisticRegressionExecutor
import io.github.nwtgck.japan_times_feature_vector.datatype.{DownloadInfo, EngDocument, JapanTimesArticle, MultiDataset}
import io.github.nwtgck.japan_times_feature_vector.downloader.{FigureAndSumoDataset, JapanTimesDataset, JapanTimesDonwloader, TimesGetterJsoup}
import io.github.nwtgck.japan_times_feature_vector.pycall.PythonCall
import io.github.nwtgck.japan_times_feature_vector.text_file_generator.{TextGeneratorForGensim, TrainAndTestFilesGenerator}
import io.github.nwtgck.japan_times_feature_vector.vector_generator.{FeatureVectorGeneratorE, Word2VecGenerator}
import org.apache.spark.{SparkConf, SparkContext}
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


    if(true){
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

    if(false) {
      // Train-Set: many titles+article, Test-Set: only title
      TrainAndTestFilesGenerator.generateTrainTestMultiSvmLightFormatFilesWithGenerator(
        JapanTimesDonwloader.`Multi-Classfiable of (train-set: many titles + article, test-set: only title)`(titleTimes = 20),
        trainFilePath = "./data/multi-class-20-titles-tfidf-word2vec-tfidf-article-train-set",
        testFilePath  = "./data/multi-class-20-titles-tfidf-word2vec-tfidf-article-test-set",
        trainSetRate  = 0.8,
        generator     = FeatureVectorGeneratorE.`generate TFIDF vec ++ (Word2Vec*TFIDF) vectors and Words`

      )
    }



    // Download Information
    val `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo` = DownloadInfo(
      storedPath = "./data/japan-times-multi-articles.json",
      pageLimit = 30,
      pageToUrls = Seq(
        TimesGetterJsoup.economyPage _,
        TimesGetterJsoup.politicPage _,
        TimesGetterJsoup.techPage _,
        TimesGetterJsoup.figurePage _,
        TimesGetterJsoup.sumoPage _
      )
    )

    // Download Information
    val `Page-Limit: 184, Articles: Economy & Politics & Crime-Legal & Editorials & Corporate` = DownloadInfo(
      storedPath = "./data/jptimes-epcec-multi-each184-articles.json",
      pageLimit = 184,
      pageToUrls = Seq(
        TimesGetterJsoup.economyPage _,
        TimesGetterJsoup.politicPage _,
        TimesGetterJsoup.crimeLegalPage _,
        TimesGetterJsoup.editorialsPage _,
        TimesGetterJsoup.corporatePage _
      )
    )

    // Download Information
    val `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo` = DownloadInfo(
      storedPath = "./data/jptimes-eptfs-multi-each46-articles.json",
      pageLimit = 46,
      pageToUrls = Seq(
        TimesGetterJsoup.economyPage _,
        TimesGetterJsoup.politicPage _,
        TimesGetterJsoup.techPage _,
        TimesGetterJsoup.figurePage _,
        TimesGetterJsoup.sumoPage _
      )
    )


    if(false){
      // Make a SparkContext
      val sparkContext = new SparkContext(new SparkConf().setAppName("JPTIMES").setMaster("local[*]"))
      val model = Word2VecGenerator
        .calcOrGetCacheModel(sparkContext = sparkContext, vectorSize = 100, jptimesFilePath = "./gensim-text-for-word2vec/jp_times_with_title.txt")

      println(model.transform("Trump"))
    }


    if(false) {
      LogisticRegressionExecutor.executeTFIDFAndWord2Vec(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        "./gensim-text-for-word2vec/jp_times_with_title.txt",
        word2VecDem = 100,
        crossValidationTimes = 10
      )
    }

    if(false) {
      LogisticRegressionExecutor.executeTFIDF(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        "./gensim-text-for-word2vec/jp_times_with_title.txt",
        crossValidationTimes = 10
      )
    }


    if(false) {
      // [Setting]
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Enable
      LogisticRegressionExecutor.`execute (Normalized TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
//        "./gensim-text-for-word2vec/jp_times_with_title.txt",
        word2VecDem = 100,
        crossValidationTimes = 10,
        enableNormalizationForTFIDF  = true,
        enableNormaliztionForWord2vec = true
      )

      // [Result] (it can be changed because of random)
//      word2vec abs max: 93.59978816879448
//      TFIDF abs max     : 166.9562181907852
//
//      Average Accuracy: 0.9594017094017093
//      Max Accuracy    : 0.9786324786324786
//      Min Accuracy    : 0.9358974358974359
    }


    if(false) {
      // [Setting]
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Enable
      LogisticRegressionExecutor.`execute (Normalized TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
//        "./gensim-text-for-word2vec/jp_times_with_title.txt",
        word2VecDem = 100,
        crossValidationTimes = 10,
        enableNormalizationForTFIDF   = false,
        enableNormaliztionForWord2vec = true
      )
      // [Result] (it can be changed because of random)
//      word2vec abs max: 93.59978816879448
//      TFIDF abs max     : 166.9562181907852
//
//      Average Accuracy: 0.9675213675213676
//      Max Accuracy    : 0.9914529914529915
//      Min Accuracy    : 0.9401709401709402
    }




    if(false) {
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute (Normalized TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
//        "./gensim-text-for-word2vec/jp_times_with_title.txt",
        word2VecDem = 100,
        crossValidationTimes = 10,
        enableNormalizationForTFIDF   = true,
        enableNormaliztionForWord2vec = false
      )

      // [Result] (it can be changed because of random)
      //    Average Accuracy: 0.9675213675213674
      //    Max Accuracy    : 0.9743589743589743
      //    Min Accuracy    : 0.9529914529914529
      //    -----------------------------------
      //    TFIDF abs max     : 166.9562181907852
      //    word2vec abs max: 93.59978816879448

    }

    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 184, Articles: Economy & Politics & Crime-Legal & Editorials & Corporate`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        crossValidationTimes = 10,
        enableNormalizationForTFIDF   = true,
        enableNormaliztionForWord2vec = false
      )


    }


    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        crossValidationTimes = 10,
        enableNormalizationForTFIDF   = true,
        enableNormaliztionForWord2vec = false
      )

//      Average Accuracy: 0.9602564102564102
//      Max Accuracy    : 0.9743589743589743
//      Min Accuracy    : 0.9401709401709402
//      -----------------------------------
//      TFIDF abx max     : 152.67710415586876
//      word2vec abs max: 89.35535305738449


    }


    if(false){
      val docs = JapanTimesDonwloader
        .`Labeled Multi-Classfiable of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 184, Articles: Economy & Politics & Crime-Legal & Editorials & Corporate`)
        .multiDataset()
        .docs
      println(s"Number of Documents: ${docs.length}")
      // 10198

    }

    if(false) {
      // [Use only Spark's TFIDF]

      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 184, Articles: Economy & Politics & Crime-Legal & Editorials & Corporate`),
        trainSetRate = 0.8,
        crossValidationTimes = 10
      )

//      Average Accuracy: 0.909705882352941
//      Max Accuracy    : 0.9245098039215687
//      Min Accuracy    : 0.8980392156862745
//      -----------------------------------
//      TFIDF abx max     : 209.20921551269367

      //
      // Accurracy is lower than `Page_limit-30` data
      // I think each category is more similar than `Page-Limit-30` data
    }


    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        crossValidationTimes = 10,
        enableNormalizationForTFIDF   = false,
        enableNormaliztionForWord2vec = false
      )

    }


    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: only title, test-set: only title)`(downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        crossValidationTimes = 10,
        enableNormalizationForTFIDF   = false,
        enableNormaliztionForWord2vec = false
      )

//      Average Accuracy: 0.8312625250501002
//      Max Accuracy    : 0.8517034068136272
//      Min Accuracy    : 0.811623246492986
//      -----------------------------------
//      TFIDF abx max     : 14.257793655251348
//      word2vec abs max: 1.1163835804909468

    }

    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Enable
      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: only title, test-set: only title)`(downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        crossValidationTimes = 10,
        enableNormalizationForTFIDF   = false,
        enableNormaliztionForWord2vec = true
      )

//      Average Accuracy: 0.8384769539078156
//      Max Accuracy    : 0.8496993987975952
//      Min Accuracy    : 0.8256513026052105
//      -----------------------------------
//      TFIDF abx max     : 14.257793655251348
//      word2vec abs max: 1.1163835804909468

    }

    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: only title, test-set: only title)`(downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        crossValidationTimes = 10,
        enableNormalizationForTFIDF   = true,
        enableNormaliztionForWord2vec = false
      )

//      Average Accuracy: 0.8314629258517032
//      Max Accuracy    : 0.8677354709418837
//      Min Accuracy    : 0.8036072144288577
//      -----------------------------------
//      TFIDF abx max     : 14.257793655251348
//      word2vec abs max: 1.1163835804909468

    }


    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: only title, test-set: only title)`(downloadInfo = `Page-Limit: 184, Articles: Economy & Politics & Crime-Legal & Editorials & Corporate`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        crossValidationTimes = 10,
        enableNormalizationForTFIDF   = false,
        enableNormaliztionForWord2vec = false
      )

    }

    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute train-test (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        enableNormalizationForTFIDF   = false,
        enableNormaliztionForWord2vec = false
      )

      //      Accuracy    : 0.7350427350427351
      //      -----------------------------------
      //      TFIDF abx max     : 245.3739173933605
      //      word2vec abs max: 88.96915444626939


    }


    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute train-test (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        enableNormalizationForTFIDF   = false,
        enableNormaliztionForWord2vec = false
      )

//      Accuracy    : 0.7194388777555111
//      -----------------------------------
//      TFIDF abx max     : 249.51138896689858
//      word2vec abs max: 109.86472121835686

    }



    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Enable
      LogisticRegressionExecutor.`execute train-test (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        enableNormalizationForTFIDF   = false,
        enableNormaliztionForWord2vec = true
      )

//      Accuracy    : 0.7454909819639278
//      -----------------------------------
//      TFIDF abx max     : 295.8309956587704
//      word2vec abs max: 133.15017612822703

    }


    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute train-test (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        enableNormalizationForTFIDF   = true,
        enableNormaliztionForWord2vec = false
      )

//      Accuracy    : 0.7595190380761523
//      -----------------------------------
//      TFIDF abx max     : 264.9340279291955
//      word2vec abs max: 85.03183344641002

    }


    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Enable
      LogisticRegressionExecutor.`execute train-test (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        enableNormalizationForTFIDF   = true,
        enableNormaliztionForWord2vec = true
      )

//      Accuracy    : 0.751503006012024
//      -----------------------------------
//      TFIDF abx max     : 273.3546682130668
//      word2vec abs max: 58.38296064088354
    }


    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Enable
      LogisticRegressionExecutor.`execute train-test (Normalized Spark's TFIDF)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        enableNormalizationForTFIDF   = true
      )
//      Accuracy    : 0.6973947895791583
//      -----------------------------------
//      TFIDF abx max     : 295.8309956587704
    }

    if(true) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Disable
      LogisticRegressionExecutor.`execute train-test (Normalized Spark's TFIDF)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        enableNormalizationForTFIDF   = false
      )

//      Accuracy    : 0.7535070140280561
//      -----------------------------------
//      TFIDF abx max     : 242.04354190263032
    }



    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Enable
      LogisticRegressionExecutor.`execute train-test (Normalized Spark's TFIDF)`(
        JapanTimesDonwloader.`Labeled Multi-Classfiable of (train-set: (article + many titles), test-set: title)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        enableNormalizationForTFIDF = true
      )

//      Accuracy    : 0.7294589178356713
//      -----------------------------------
//      TFIDF abx max     : 260.35913929858793

    }



  }
}
