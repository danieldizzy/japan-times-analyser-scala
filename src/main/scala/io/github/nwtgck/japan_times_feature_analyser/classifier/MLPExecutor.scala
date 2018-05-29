package io.github.nwtgck.japan_times_feature_analyser.classifier

import breeze.linalg.DenseVector
import breeze.stats.distributions.Rand
import io.github.nwtgck.japan_times_feature_analyser.datatype._
import io.github.nwtgck.japan_times_feature_analyser.vector_generator.{FeatureVectorGeneratorE, Word2VecGenerator}
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Ryo on 2017/01/26.
  */
object MLPExecutor {
  /**
    * Execute (Normalized TFIDF) ++ (Normalized Word2Vec) by Logistic Regression
    *
    * @param dataset
    * @param trainSetRate
    */
//  AAA
  def `execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(dataset: LabeledMultiDataset[Int], trainSetRate: Double, word2VecDem: Int, crossValidationTimes: Int, enableNormalizationForTFIDF: Boolean, enableNormaliztionForWord2vec: Boolean): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)
    // Number of crossvalidation times > 0
    require(crossValidationTimes > 0)


    // extract the positive docs and negative docs
    val LabeledMultiDataset(docs, classNum) = dataset

    val sparkSession: SparkSession = SparkSession
      .builder()
      .appName("JPTIMES").config("spark.executor.memory", "8g").config("spark.driver.memory", "8g").master("local[*]")
      .getOrCreate()

    import sparkSession.implicits._

    // Make a SparkContext
//    val sparkContext = new SparkContext(new SparkConf().setAppName("JPTIMES").set("spark.executor.memory", "8g").set("spark.driver.memory", "8g").setMaster("local[*]"))
    val sparkContext = sparkSession.sparkContext


    // Get the feature vectors and all words containing all documents
    val (tfidfFeatureVectors, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWordsWithSparkContext(sparkContext, docs)

    println(s"number of words: ${allWords.size}")

    // Map for word to vector
    val wordToVec: Map[Word, DenseVector[Double]] = {
      // word2vec model
      //      val word2VecModel = Word2VecGenerator.calcOrGetCacheModel(sparkContext = sparkContext, vectorSize = word2VecDem, jptimesFilePath = jptimesFilePath)
      val word2VecModel = Word2VecGenerator.calcOrGetCacheModelByAllDocs(sparkContext = sparkContext, vectorSize = word2VecDem, numIterations = 1, allDocuments = docs)
      // All zero vector
      val allZeroVec    = DenseVector.zeros[Double](word2VecDem)
      // all words for word2vec (Word2Vec ignore some words which don't appear frequently)
      val allWordsForWord2Vec = word2VecModel.getVectors.keySet.map(Word)
      // word exist nor not
      def wordExist(word: Word): Boolean = {
        allWordsForWord2Vec.contains(word)
      }

      allWords.map { word =>
        // if a word exist
        val vec = if(wordExist(word)) {
          new DenseVector(
            word2VecModel
              .transform(word.entity)
              .toArray
          )
        } else {
          allZeroVec
        }
        (word, vec)
      }.toMap
    }

    // Map for document to accumulated word2vec
    val docToAccumedWord2Vec = {
      for {
        doc <- docs
        word2vecFeatures = doc.wordsSet.map(wordToVec).reduce(_ :+ _) // add a element with another element
      } yield (doc, word2vecFeatures)
    }.toMap

    // Absolute-max element in all Word2Vec vectors for normalization
    val word2VecAbsMaxElem: Double = docToAccumedWord2Vec.values.map(vec => vec.toArray.maxBy(e => e.abs)).max
    println(s"word2vec abs max: ${word2VecAbsMaxElem}")


    // Absolute-max element in all IFIDF vectors for normalization
    val tfidfAbsMaxElem = tfidfFeatureVectors.values.map(vec => vec.toArray.maxBy(_.abs)).max
    println(s"IFIDF abx max: ${tfidfAbsMaxElem}")

    // Make exmaples (I think `Example` means labeled document(?))
    val labeledPoints = {
      for{
        doc <- docs
        label = doc.label-1 // Why -1? Because Spark needs label 0 to (n-1) for n classification
      } yield {
        // TFIDF vector
        val tfidfFeatures = tfidfFeatureVectors(doc)
        val tfidfFeatureStream = (0 until tfidfFeatures.size).toStream.map(i => tfidfFeatures(i))
        val normalizedTfidfFeatures = tfidfFeatureStream.map(_ / tfidfAbsMaxElem)
        // word2vec vector
        val word2vec: DenseVector[Double] = docToAccumedWord2Vec(doc)
        val word2vecStream = (0 until word2vec.size).toStream.map(i => word2vec(i))
        // Normalized Word Vector
        val normalizedWord2vecFeatures = word2vecStream.map(_ / word2VecAbsMaxElem)

        val usedTfidfFeatures     = if(enableNormalizationForTFIDF) normalizedTfidfFeatures else tfidfFeatureStream
        val usedWord2vecFeatures  = if(enableNormaliztionForWord2vec) normalizedWord2vecFeatures else word2vecStream
        (
          label,
          new org.apache.spark.ml.linalg.DenseVector((usedTfidfFeatures ++ usedWord2vecFeatures).toArray)
        )
      }

    }

    val inSize  = labeledPoints.head._2.size
    val outSize = labeledPoints.toStream.map(_._1).distinct.size
    val trainer = new MultilayerPerceptronClassifier()
      .setLayers(Array(inSize, 10, outSize))
      .setBlockSize(128)
      .setSeed(1234L)

    val allData = sparkSession.createDataFrame(labeledPoints).toDF("label", "features")
    val model = trainer.fit(
      allData
    )

    val result = model.transform(allData)
    val predictionAndLabels = result.select("prediction", "label")
    val evaluator = new MulticlassClassificationEvaluator()
      .setMetricName("accuracy")

    println(s"Test set accuracy = ${evaluator.evaluate(predictionAndLabels)}")



  //    // Accuracies for cross validation
//    val accuracies: Seq[Double] = (1 to crossValidationTimes).map { times =>
//      // Split the vectors into vectors for train and vectors for test
//      val (trainVecs, testVecs) =
//        Rand.permutation(labeledPoints.length).draw.map(labeledPoints) // Shuffle the vectors
//          .splitAt((labeledPoints.length * trainSetRate).toInt) // Split into train and test
//
//      // Print the number of total and train and test
//      println(s"Total: ${labeledPoints.length}, Train: ${trainVecs.length}, Test: ${testVecs.length}")
//
//
//      val trainRdd = sparkContext.makeRDD(trainVecs)
//      val testRdd = sparkContext.makeRDD(testVecs)
//
//
//      println("Train starting...")
//
//      // Run training algorithm to build the model
//      val model = new LogisticRegressionWithLBFGS()
//        .setNumClasses(classNum)
//        .run(trainRdd)
//
//      println("Train finished!")
//
//      // Compute raw scores on the test set.
//      val predictionAndLabels = testRdd.map { case LabeledPoint(label, features) =>
//        val prediction = model.predict(features)
//        (prediction, label)
//      }
//
//      // Get evaluation metrics.
//      val metrics = new MulticlassMetrics(predictionAndLabels)
//      val accuracy = metrics.accuracy
//      println(s"Accuracy(${times} times) = $accuracy")
//      accuracy
//    }

//    println(s"Average Accuracy: ${accuracies.sum / accuracies.length}")
//    println(s"Max Accuracy    : ${accuracies.max}")
//    println(s"Min Accuracy    : ${accuracies.min}")
//    println(s"-----------------------------------")
//    println(s"TFIDF abx max     : ${tfidfAbsMaxElem}")
//    println(s"word2vec abs max: ${word2VecAbsMaxElem}")


    sparkSession.stop()
  }


}
