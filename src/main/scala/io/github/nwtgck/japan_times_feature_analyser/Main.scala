package io.github.nwtgck.japan_times_feature_analyser

import io.github.nwtgck.japan_times_feature_analyser.classifier.{LogisticRegressionExecutor, SVMExecutor}
import io.github.nwtgck.japan_times_feature_analyser.datatype._
import io.github.nwtgck.japan_times_feature_analyser.downloader.{FigureAndSumoDataset, JapanTimesDataset, JapanTimesDonwloader, TimesGetterJsoup}
import io.github.nwtgck.japan_times_feature_analyser.pycall.PythonCall
import io.github.nwtgck.japan_times_feature_analyser.text_file_generator.{TextGeneratorForGensim, TrainAndTestFilesGenerator}
import io.github.nwtgck.japan_times_feature_analyser.vector_generator.{FeatureVectorGeneratorE, Word2VecGenerator}
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
      // *** Binary Classification for SVM-Light ***

      // make train-set file and test-set file for SVM light
      TrainAndTestFilesGenerator.generateSvmLightFormatFiles(
        FigureAndSumoDataset.binaryDataset(),
        trainFilePath = "./data/train-set",
        testFilePath = "./data/test-set",
        trainSetRate = 0.8
      )
//      $ ./svm_learn train-set model
//      $ ./svm_classify test-set model
//        Reading model...OK. (196 support vectors read)
//      Classifying test examples..100..done
//      Runtime (without IO) in cpu-seconds: 0.00
//      Accuracy on test set: 100.00% (122 correct, 0 incorrect, 122 total)
//      Precision/recall on test set: 100.00%/100.00%
    }


    if(false) {
      // *** Multi Classification for SVM-Light ***

      TrainAndTestFilesGenerator.generateMultiSvmLightFormatFiles(
        JapanTimesDataset.multiDataset(),
        trainFilePath = "./data/multi-class-train-set",
        testFilePath  =  "./data/multi-class-test-set",
        trainSetRate  = 0.8
      )

//      $ ./svm_multiclass_learn -c 1000 multi-class-train-set model
//      $ ./svm_multiclass_classify multi-class-test-set model
//        Reading model...done.
//        Reading test examples... (255 examples) done.
//      Classifying test examples...done
//      Runtime (without IO) in cpu-seconds: 0.00
//      Average loss on test set: 1.9608
//      Zero/one-error on test set: 1.96% (250 correct, 5 incorrect, 255 total)
    }


    if(false) {
      // *** Average length of all titles ***

      val MultiDataset(seq) = JapanTimesDonwloader.onlyTitleMultiDataset
      val lengths = seq.flatMap(_.map(_.entity.length))
      val average = lengths.sum.toDouble / lengths.length
      println("Average length of titles " + average) // 55.72
    }


    if(false) {
      // *** Multi Classification for SVM-Light ***

      // Train-Set: only title, Test-Set: only title
      TrainAndTestFilesGenerator.generateMultiSvmLightFormatFiles(
        dataset = JapanTimesDonwloader.onlyTitleMultiDataset,
        trainFilePath = "./data/multi-class-only-title-train-set",
        testFilePath  = "./data/multi-class-only-title-test-set",
        trainSetRate  = 0.8
      )
//      $ ./svm_multiclass_learn -c 1000 multi-class-only-title-train-set  model
//      $ ./svm_multiclass_classify multi-class-only-title-test-set  model
//        Reading model...done.
//        Reading test examples... (237 examples) done.
//      Classifying test examples...done
//      Runtime (without IO) in cpu-seconds: 0.00
//      Average loss on test set: 12.6582
//      Zero/one-error on test set: 12.66% (207 correct, 30 incorrect, 237 total)

    }



    if(false) {
      // *** Multi Classification for SVM-Light ***

      // Train-Set: title+article, Test-Set: only title
      TrainAndTestFilesGenerator.generateTrainTestMultiSvmLightFormatFiles(
        dataset = JapanTimesDonwloader.`Multi-Dataset of (train-set: title + article, test-set: only title)`,
        trainFilePath = "./data/multi-class-title-article-train-set",
        testFilePath = "./data/multi-class-title-article-test-set",
        trainSetRate = 0.8
      )

//      $ ./svm_multiclass_learn -c 1000 multi-class-title-article-train-set  model
//      $ ./svm_multiclass_classify  multi-class-title-article-test-set  model
//        Reading model...done.
//        Reading test examples... (237 examples) done.
//      Classifying test examples...done
//      Runtime (without IO) in cpu-seconds: 0.00
//      Average loss on test set: 10.5485
//      Zero/one-error on test set: 10.55% (212 correct, 25 incorrect, 237 total)
    }


    if(false){
      // *** Multi Classification for SVM-Light ***

      // Train: many titles+article
      // Test : only title
      TrainAndTestFilesGenerator.generateTrainTestMultiSvmLightFormatFiles(
        dataset = JapanTimesDonwloader.`Multi-Dataset of (train-set: many titles + article, test-set: only title)`(titleTimes = 20),
        trainFilePath = "./data/multi-class-20-titles-article-train-set",
        testFilePath  = "./data/multi-class-20-titles-article-test-set",
        trainSetRate  = 0.8
      )
//      $ ./svm_multiclass_learn -c 1000 multi-class-20-titles-article-train-set  model
//      $ ./svm_multiclass_classify multi-class-20-titles-article-test-set  model
//        Reading model...done.
//        Reading test examples... (237 examples) done.
//      Classifying test examples...done
//      Runtime (without IO) in cpu-seconds: 0.00
//      Average loss on test set: 8.4388
//      Zero/one-error on test set: 8.44% (217 correct, 20 incorrect, 237 total)
    }


    if(false) {
      // Generate article text from article data
      TextGeneratorForGensim.generateTextsSeparatedByGroups(dirPath = "./gensim-text")
      // (gensim is a python library)
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
      // Sample Code of pycall
      // (pycall is my python library which connect with python and other language)

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
      // Train: article
      // Test : article
      // make train-set file and test-set file (TFIDF & word2vec collaboration) for SVM light
      TrainAndTestFilesGenerator.generateMultiSvmLightFormatFilesWithGenerator(
        JapanTimesDataset.multiDataset(),
        trainFilePath = "./data/multi-tfidf-word2vec-train-set",
        testFilePath  = "./data/multi-tfidf-word2vec-test-set",
        trainSetRate  = 0.8,
        generator     = FeatureVectorGeneratorE.`generate TFIDF & Word2Vec vectors and Words`
      )

//      $ ./svm_multiclass_learn -c 1000 multi-tfidf-word2vec-train-set  model
//      $ ./svm_multiclass_classify multi-tfidf-word2vec-test-set  model
//        Reading model...done.
//        Reading test examples... (255 examples) done.
//      Classifying test examples...done
//      Runtime (without IO) in cpu-seconds: 0.00
//      Average loss on test set: 1.1765
//      Zero/one-error on test set: 1.18% (252 correct, 3 incorrect, 255 total)

    }


    if(false){
      // Generate one big article text from article data with title
      TextGeneratorForGensim.generateOneBigTextWithTitle(dirPath = "./gensim-text-for-word2vec")
    }


    if(false) {
      // Train: many titles+article
      // Test : only title
      TrainAndTestFilesGenerator.generateTrainTestMultiSvmLightFormatFilesWithGenerator(
        dataset = JapanTimesDonwloader.`Multi-Dataset of (train-set: many titles + article, test-set: only title)`(titleTimes = 20),
        trainFilePath = "./data/multi-class-20-titles-tfidf-word2vec-article-train-set",
        testFilePath  = "./data/multi-class-20-titles-tfidf-word2vec-article-test-set",
        trainSetRate  = 0.8,
        generator     = FeatureVectorGeneratorE.`generate TFIDF & Word2Vec vectors and Words`
      )
//      $ ./svm_multiclass_learn -c 1000 multi-class-20-titles-tfidf-word2vec-article-train-set  model
//      $ ./svm_multiclass_classify multi-class-20-titles-tfidf-word2vec-article-test-set  model
//        Reading model...done.
//        Reading test examples... (237 examples) done.
//      Classifying test examples...done
//      Runtime (without IO) in cpu-seconds: 0.00
//      Average loss on test set: 54.8523
//      Zero/one-error on test set: 54.85% (107 correct, 130 incorrect, 237 total)

    }

    if(false) {
      // Train: many titles+article
      // Test : only title
      TrainAndTestFilesGenerator.generateTrainTestMultiSvmLightFormatFilesWithGenerator(
        dataset = JapanTimesDonwloader.`Multi-Dataset of (train-set: many titles + article, test-set: only title)`(titleTimes = 20),
        trainFilePath = "./data/multi-class-20-titles-tfidf-word2vec-tfidf-article-train-set",
        testFilePath  = "./data/multi-class-20-titles-tfidf-word2vec-tfidf-article-test-set",
        trainSetRate  = 0.8,
        generator     = FeatureVectorGeneratorE.`generate TFIDF vec ++ (Word2Vec*TFIDF) vectors and Words`

      )

//      $ ./svm_multiclass_learn -c 1000 multi-class-20-titles-tfidf-word2vec-tfidf-article-train-set  model
//      $ ./svm_multiclass_classify multi-class-20-titles-tfidf-word2vec-tfidf-article-test-set  model
//        Reading model...done.
//        Reading test examples... (237 examples) done.
//      Classifying test examples...done
//      Runtime (without IO) in cpu-seconds: 0.00
//      Average loss on test set: 25.7384
//      Zero/one-error on test set: 25.74% (176 correct, 61 incorrect, 237 total)
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

    // Download Information
    val `Page-Limit: 10, Articles: Economy & Figure` = DownloadInfo(
      storedPath = "./data/jptimes-eptfs-multi-each10-articles.json",
      pageLimit = 10,
      pageToUrls = Seq(
        TimesGetterJsoup.economyPage _,
//        TimesGetterJsoup.politicPage _,
//        TimesGetterJsoup.techPage _,
        TimesGetterJsoup.figurePage _
//        TimesGetterJsoup.sumoPage _
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
      // Train: article
      // Test : article
      LogisticRegressionExecutor.executeTFIDFAndWord2Vec(
        dataset = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        "./gensim-text-for-word2vec/jp_times_with_title.txt",
        word2VecDem = 100,
        crossValidationTimes = 10
      )
    }

    if(false) {
      // Train: article
      // Test : article
      LogisticRegressionExecutor.executeTFIDF(
        dataset = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        "./gensim-text-for-word2vec/jp_times_with_title.txt",
        crossValidationTimes = 10
      )
    }


    if(false) {
      // [Setting]

      // Train: article
      // Test : article
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Enable
      LogisticRegressionExecutor.`execute (Normalized TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
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

      // Train: article
      // Test : article
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Enable
      LogisticRegressionExecutor.`execute (Normalized TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
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
      // Train: article
      // Test : article
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute (Normalized TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
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
      // Train: article
      // Test : article
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 184, Articles: Economy & Politics & Crime-Legal & Editorials & Corporate`),
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
      // Train: article
      // Test : article
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo`),
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
        .`Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 184, Articles: Economy & Politics & Crime-Legal & Editorials & Corporate`)
        .docs
      println(s"Number of Documents: ${docs.length}")
      // 10198

    }

    if(false) {
      // [Use only Spark's TFIDF]

      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF)`(
        dataset = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 184, Articles: Economy & Politics & Crime-Legal & Editorials & Corporate`),
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
      // Train: article
      // Test : article
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        crossValidationTimes = 10,
        enableNormalizationForTFIDF   = false,
        enableNormaliztionForWord2vec = false
      )
//      Average Accuracy: 0.9681362725450902
//      Max Accuracy    : 0.9739478957915831
//      Min Accuracy    : 0.9599198396793587
//      -----------------------------------
//      TFIDF abx max     : 176.928060220864
//      word2vec abs max: 136.46763747135992

    }


    if(false) {
      // [Use Spark's TFIDF]
      //
      // Train: title
      // Test : title
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only title, test-set: only title)`(downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
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
      // Train: title
      // Test : title
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Enable
      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only title, test-set: only title)`(downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
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
      // Train: title
      // Test : title
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only title, test-set: only title)`(downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
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
      // Train: title
      // Test : title
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only title, test-set: only title)`(downloadInfo = `Page-Limit: 184, Articles: Economy & Politics & Crime-Legal & Editorials & Corporate`),
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
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 30, Articles: Economy & Politic & Tech & Figure & Sumo`),
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
      // [Not use Spark's TFIDF]
      //
      // TFIDF Normalization   : Disable
      LogisticRegressionExecutor.`execute train-test (Normalized TFIDF)`(
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        enableNormalizationForTFIDF   = false
      )
      //      Accuracy    : 0.7835671342685371
      //      -----------------------------------
      //      TFIDF abx max     : 313.6538216099216
    }


    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute train-test (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
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
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
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
      // [Not use Spark's TFIDF]
      //
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Enable
      LogisticRegressionExecutor.`execute train-test (Normalized TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        word2VecNumIterations = 1,
        enableNormalizationForTFIDF   = false,
        enableNormaliztionForWord2vec = true
      )
//      Accuracy    : 0.8056112224448898
//      -----------------------------------
//      TFIDF abx max     : 344.1522975545592
//      word2vec abs max: 138.42150734065217

    }


    if(false) {
      // [Not use Spark's TFIDF]
      //
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Enable
      LogisticRegressionExecutor.`execute train-test (Normalized TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        word2VecNumIterations = 100,
        enableNormalizationForTFIDF   = false,
        enableNormaliztionForWord2vec = true
      )

    }

    if(false) {
      // [Not use Spark's TFIDF and Additional docs for word2vec]
      //
      // TFIDF Normalization   : Disable
      // Word2vec Normalization: Enable

      val LabeledMultiDataset(additionalDocs, classNum) = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(`Page-Limit: 184, Articles: Economy & Politics & Crime-Legal & Editorials & Corporate`)
      LogisticRegressionExecutor.`execute train-test (Normalized TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        word2VecNumIterations = 1,
        enableNormalizationForTFIDF   = false,
        enableNormaliztionForWord2vec = true,
        additionalDocsForWord2vec     = additionalDocs
      )
//      Accuracy    : 0.8096192384769539
//      -----------------------------------
//      TFIDF abx max     : 313.6538216099216
//      word2vec abs max: 180.10421691666124

    }

    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute train-test (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
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
      // [Not use Spark's TFIDF]
      //
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Disable
      LogisticRegressionExecutor.`execute train-test (Normalized TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        word2VecNumIterations = 1,
        enableNormalizationForTFIDF   = true,
        enableNormaliztionForWord2vec = false
      )
//      Accuracy    : 0.7935871743486974
//      -----------------------------------
//      TFIDF abx max     : 313.6538216099216
//      word2vec abs max: 92.43100340146339

    }

    if(false) {
      // [Not use Spark's TFIDF and Additional docs for word2vec]
      //
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Disable

      val LabeledMultiDataset(additionalDocs, classNum) = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(`Page-Limit: 184, Articles: Economy & Politics & Crime-Legal & Editorials & Corporate`)
      LogisticRegressionExecutor.`execute train-test (Normalized TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        word2VecNumIterations = 1,
        enableNormalizationForTFIDF   = true,
        enableNormaliztionForWord2vec = false,
        additionalDocsForWord2vec     = additionalDocs
      )
//      Accuracy    : 0.8096192384769539
//      -----------------------------------
//      TFIDF abx max     : 344.1522975545592
//      word2vec abs max: 91.04199107439217
    }


    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Enable
      LogisticRegressionExecutor.`execute train-test (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
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
      // [Not use Spark's TFIDF]
      //
      // TFIDF Normalization   : Enable
      // Word2vec Normalization: Enable
      LogisticRegressionExecutor.`execute train-test (Normalized TFIDF) ++ (Normalized Word2Vec)`(
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        word2VecDem = 100,
        word2VecNumIterations = 1,
        enableNormalizationForTFIDF   = true,
        enableNormaliztionForWord2vec = true
      )
//      Accuracy    : 0.7675350701402806
//      -----------------------------------
//      TFIDF abx max     : 344.1522975545592
//      word2vec abs max: 66.07668686308898
    }



      if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Enable
      LogisticRegressionExecutor.`execute train-test (Normalized Spark's TFIDF)`(
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        enableNormalizationForTFIDF   = true
      )
//      Accuracy    : 0.6973947895791583
//      -----------------------------------
//      TFIDF abx max     : 295.8309956587704
    }

    if(false) {
      // [Use Spark's TFIDF]
      //
      // TFIDF Normalization   : Disable
      LogisticRegressionExecutor.`execute train-test (Normalized Spark's TFIDF)`(
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: many titles)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
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
        dataset = JapanTimesDonwloader.`Labeled Dataset of (train-set: (article + many titles), test-set: title)`(titleTimes = 20, downloadInfo = `Page-Limit: 46, Articles: Economy & Politic & Tech & Figure & Sumo`),
        trainSetRate = 0.8,
        enableNormalizationForTFIDF = true
      )

//      Accuracy    : 0.7294589178356713
//      -----------------------------------
//      TFIDF abx max     : 260.35913929858793

    }

    if(false){
      val LabeledMultiDataset(docs, classNum) = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(`Page-Limit: 184, Articles: Economy & Politics & Crime-Legal & Editorials & Corporate`)
      val sparkContext = new SparkContext(new SparkConf().setAppName("JPTIMES").setMaster("local[*]"))
      val word2VecModel = Word2VecGenerator.calcOrGetCacheModelByAllDocs(sparkContext = sparkContext, vectorSize = 100, allDocuments = docs, numIterations = 1)
      println(word2VecModel.findSynonyms("Trump", 10).toList)
      println(word2VecModel.findSynonyms("Japan", 10).toList)
      println(word2VecModel.findSynonyms("Abe", 10).toList)
      println(word2VecModel.findSynonyms("is", 10).toList)
    }

    if(true) {
      // Train: article
      // Test : article
      val dataset: LabeledMultiDataset[Int] = JapanTimesDonwloader.`Labeled Multi-Dataset of (train-set: only article, test-set: only article)`(downloadInfo = `Page-Limit: 10, Articles: Economy & Figure`)
      SVMExecutor.executeTFIDF(
        dataset = dataset,
        trainSetRate = 0.3,
        numIterations = 100,
        crossValidationTimes = 5
      )
      println(s"The number of articles: ${dataset.docs.length}")
    }



  }
}
