package io.github.nwtgck.japan_times_feature_vector

import breeze.linalg.DenseVector
import breeze.stats.distributions.Rand
import io.github.nwtgck.japan_times_feature_vector.datatype.{LabeledMultiClassifiable, LabeledMultiDataset, Word}
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Ryo on 2017/01/24.
  */
object NakSVMExecutor {
  /**
    * Execute only TFIDF by SVM
    *
    * @param multiClassifiable
    * @param trainSetRate
    */
  @deprecated (message = "This method causes the `NoSuchMethodError`")
  def executeSVM[L](multiClassifiable: LabeledMultiClassifiable[L], trainSetRate: Double): Unit = {

//    /** [[trainSetRate]] should be between 0.0 and 1.0 */
//    require(0 <= trainSetRate && trainSetRate <= 1)
//
//
//    // extract the positive docs and negative docs
//    val LabeledMultiDataset(docs) = multiClassifiable.multiDataset()
//
//
//    // Get the feature vectors and all words containing all documents
//    val (featureVectors, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWords(docs)
//
//    println(s"number of words: ${allWords.size}")
//
//    // Make exmaples (I think `Example` means labeled document(?))
//    val examples: Seq[Example[Int, breeze.linalg.Vector[Double]]] = for{
//      (doc, index) <- docs.zipWithIndex
//      label = index + 1
//    } yield Example(label = label, features = SparseVector[Double](featureVectors(doc)))
//
//    // Split the vectors into vectors for train and vectors for test
//    val (trainVecs, testVecs) =
//    Rand.permutation(examples.length).draw.map(examples) // Shuffle the vectors
//      .splitAt((examples.length*trainSetRate).toInt)     // Split into train and test
//
//    // Print the number of total and train and test
//    println(s"Total: ${examples.length}, Train: ${trainVecs.length}, Test: ${testVecs.length}")
//
//
//    println("Train starting...")
//    val trainer = new SVM.SMOTrainer[Int, breeze.linalg.Vector[Double]](maxIterations = 1000) with Logging
//    val classifier = trainer.train(trainVecs)
//    println("Train finished!")
//
//
//    for( ex <- testVecs) {
//      val guessed = classifier.classify(ex.features)
//      println(guessed,ex.label)
//    }
//
//    // Number of correct
//    val correctNum = testVecs.count{ex =>
//      val guessed = classifier.classify(ex.features)
//      guessed == ex.label
//    }
//
//    // Print the accuracy
//    println(s"Accuracy: ${correctNum.toDouble / testVecs.length}")

  }


//  /**
//    * Execute TFIDF ++ Word2Vec by SVM
//    *
//    * @param multiClassifiable
//    * @param trainSetRate
//    */
//  def executeTFIDFAndWord2Vec[L](multiClassifiable: LabeledMultiClassifiable[L], trainSetRate: Double, jptimesFilePath: String): Unit = {
//
//    /** [[trainSetRate]] should be between 0.0 and 1.0 */
//    require(0 <= trainSetRate && trainSetRate <= 1)
//
//
//    // extract the positive docs and negative docs
//    val LabeledMultiDataset(docs) = multiClassifiable.multiDataset()
//
//
//    // Get the feature vectors and all words containing all documents
//    val (svmFeatures, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWords(docs)
//
//    println(s"number of words: ${allWords.size}")
//
//
//    // Make exmaples (I think `Example` means labeled document(?))
//    val examples: Seq[Example[Int, breeze.linalg.Vector[Double]]] = {
//
//      // Demention of the vector
//      val vecDem        = 100
//      // word2vec model
//      val word2VecModel = Word2VecGenerator.calcOrGetCacheModel(vectorSize = vecDem, jptimesFilePath = jptimesFilePath)
//      // All zero vector
//      val allZeroVec    = DenseVector.zeros[Double](vecDem)
//      // all words
//      val allWords = word2VecModel.getVectors.keySet
//      // word exist nor not
//      def wordExist(word: Word): Boolean = allWords.contains(word.entity)
//
//      for{
//        (doc, index) <- docs.zipWithIndex
//        label            = index + 1
//        tfidfFeatures    = DenseVector[Double](svmFeatures(doc))
//        word2vecFeatures = doc.wordsSet.map{word =>
//          // if a word exist
//          if(wordExist(word)) {
//            new DenseVector[Double](
//              word2VecModel
//                .transform(word.entity)
//                .toArray
//            )
//          } else {
//            allZeroVec
//          }
//        }.reduce(_ :+ _) // add a element with another element
//      } yield Example(label = label, features = new DenseVector[Double](tfidfFeatures.toArray ++ word2vecFeatures.toArray))
//    }
//
//    // Split the vectors into vectors for train and vectors for test
//    val (trainVecs, testVecs) =
//      Rand.permutation(examples.length).draw.map(examples) // Shuffle the vectors
//        .splitAt((examples.length*trainSetRate).toInt)     // Split into train and test
//
//    // Print the number of total and train and test
//    println(s"Total: ${examples.length}, Train: ${trainVecs.length}, Test: ${testVecs.length}")
//
//
//    println("Train starting...")
//    val trainer = new SVM.SMOTrainer[Int, breeze.linalg.Vector[Double]](maxIterations = 1000) with Logging
//    val classifier = trainer.train(trainVecs)
//    println("Train finished!")
//
//
//    for( ex <- testVecs) {
//      val guessed = classifier.classify(ex.features)
//      println(guessed,ex.label)
//    }
//
//    // Number of correct
//    val correctNum = testVecs.count{ex =>
//      val guessed = classifier.classify(ex.features)
//      guessed == ex.label
//    }
//
//    // Print the accuracy
//    println(s"Accuracy: ${correctNum.toDouble / testVecs.length}")
//
//  }

  /**
    * Execute TFIDF ++ Word2Vec by SVM
    *
    * @param multiClassifiable
    * @param trainSetRate
    */
  @deprecated (message = "This method causes error becase spark's SVM only care about binary classication")
  def executeTFIDFAndWord2Vec[L](multiClassifiable: LabeledMultiClassifiable[L], trainSetRate: Double, jptimesFilePath: String): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)


    // extract the positive docs and negative docs
    val LabeledMultiDataset(docs, classNum) = multiClassifiable.multiDataset()


    // Get the feature vectors and all words containing all documents
    val (svmFeatures, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWords(docs)

    println(s"number of words: ${allWords.size}")


    // Make a SparkContext
    val sparkContext = new SparkContext(new SparkConf().setAppName("JPTIMES").setMaster("local[*]"))

    // Make exmaples (I think `Example` means labeled document(?))
    val labeledPoints: Seq[LabeledPoint] = {

      // Demention of the vector
      val vecDem        = 100
      // word2vec model
      val word2VecModel = Word2VecGenerator.calcOrGetCacheModel(sparkContext = sparkContext, vectorSize = vecDem, jptimesFilePath = jptimesFilePath)
      // All zero vector
      val allZeroVec    = DenseVector.zeros[Double](vecDem)
      // all words
      val allWords = word2VecModel.getVectors.keySet
      // word exist nor not
      def wordExist(word: Word): Boolean = allWords.contains(word.entity)

      for{
        (doc, index) <- docs.zipWithIndex
        label            = index + 1
        tfidfFeatures    = DenseVector[Double](svmFeatures(doc))
        word2vecFeatures = doc.wordsSet.map{word =>
          // if a word exist
          if(wordExist(word)) {
            new DenseVector[Double](
              word2VecModel
                .transform(word.entity)
                .toArray
            )
          } else {
            allZeroVec
          }
        }.reduce(_ :+ _) // add a element with another element
      } yield LabeledPoint(label = label.toDouble, features = new org.apache.spark.mllib.linalg.DenseVector(tfidfFeatures.toArray ++ word2vecFeatures.toArray))
    }

    // Split the vectors into vectors for train and vectors for test
    val (trainVecs, testVecs) =
      Rand.permutation(labeledPoints.length).draw.map(labeledPoints) // Shuffle the vectors
        .splitAt((labeledPoints.length*trainSetRate).toInt)     // Split into train and test

    // Print the number of total and train and test
    println(s"Total: ${labeledPoints.length}, Train: ${trainVecs.length}, Test: ${testVecs.length}")

//    val sc = new SparkContext("local", "SVM sample", "/opt/cloudera/parcels/SPARK/")

    val trainRdd = sparkContext.makeRDD(trainVecs)
    val testRdd  = sparkContext.makeRDD(testVecs)


    println("Train starting...")
    val model = SVMWithSGD.train(trainRdd, 100)
    println("Train finished!")



    // Compute number of correct
    val correctNum = testRdd.map { point =>
      val prediction = model.predict(point.features)
      prediction == point.label
    }.collect().count(identity)

    println(s"Accuracy: ${correctNum.toDouble / testVecs.length}")

  }
}
