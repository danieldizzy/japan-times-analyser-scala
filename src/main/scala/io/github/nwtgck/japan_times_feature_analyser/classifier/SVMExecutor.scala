package io.github.nwtgck.japan_times_feature_analyser.classifier

import breeze.linalg.DenseVector
import breeze.stats.distributions.Rand
import io.github.nwtgck.japan_times_feature_analyser.datatype.LabeledMultiDataset
import io.github.nwtgck.japan_times_feature_analyser.vector_generator.FeatureVectorGeneratorE
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.classification.{LogisticRegressionWithLBFGS, SVMWithSGD}
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.regression.LabeledPoint

object SVMExecutor {
  /**
    *
    * @param dataset
    * @param trainSetRate
    * @param crossValidationTimes
    */
  def executeTFIDF(dataset: LabeledMultiDataset[Int], trainSetRate: Double, numIterations: Int, crossValidationTimes: Int): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)
    // Number of crossvalidation times > 0
    require(crossValidationTimes > 0)


    // extract the positive docs and negative docs
    val LabeledMultiDataset(docs, classNum) = dataset


    // Get the feature vectors and all words containing all documents
    val (svmFeatures, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWords(docs)

    println(s"number of words: ${allWords.size}")


    // Make a SparkContext
    val sparkContext = new SparkContext(new SparkConf().setAppName("JPTIMES").setMaster("local[*]"))

    // Make exmaples (I think `Example` means labeled document(?))
    val labeledPoints: Seq[LabeledPoint] = {
      for{
        doc <- docs
        label = doc.label-1 // Why -1? Because Spark needs label 0 to (n-1) for n classification
        tfidfFeatures    = DenseVector[Double](svmFeatures(doc))
      } yield LabeledPoint(label = label.toDouble, features = new org.apache.spark.mllib.linalg.DenseVector(tfidfFeatures.toArray))

    }


    // Accuracies for cross validation
    val accuracies: Seq[Double] = (1 to crossValidationTimes).map { times =>
      // Split the vectors into vectors for train and vectors for test
      val (trainVecs, testVecs) =
        Rand.permutation(labeledPoints.length).draw.map(labeledPoints) // Shuffle the vectors
          .splitAt((labeledPoints.length * trainSetRate).toInt) // Split into train and test

      // Print the number of total and train and test
      println(s"Total: ${labeledPoints.length}, Train: ${trainVecs.length}, Test: ${testVecs.length}")


      val trainRdd = sparkContext.makeRDD(trainVecs)
      val testRdd = sparkContext.makeRDD(testVecs)


      println("Train starting...")

      // Run training algorithm to build the model
      val model = SVMWithSGD.train(trainRdd, numIterations = numIterations)

      println("Train finished!")

      // Compute raw scores on the test set.
      val predictionAndLabels = testRdd.map { case LabeledPoint(label, features) =>
        val prediction = model.predict(features)
        (prediction, label)
      }

      // Get evaluation metrics.
      val metrics = new MulticlassMetrics(predictionAndLabels)
      val accuracy = metrics.accuracy
      println(s"Accuracy(${times} times) = $accuracy")
      accuracy
    }

    println(s"Average Accuracy: ${accuracies.sum / accuracies.length}")

    sparkContext.stop()
  }
}
