package io.github.nwtgck.japan_times_feature_analyser.classifier

import breeze.linalg.DenseVector
import breeze.stats.distributions.Rand
import io.github.nwtgck.japan_times_feature_analyser.datatype._
import io.github.nwtgck.japan_times_feature_analyser.vector_generator.{FeatureVectorGeneratorE, Word2VecGenerator}
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS
import org.apache.spark.mllib.evaluation.MulticlassMetrics
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Ryo on 2017/01/26.
  */
object LogisticRegressionExecutor {
  /**
    * Execute only TFIDF by Logistic Regression
    *
    * @param dataset
    * @param trainSetRate
    */
  def executeTFIDF(dataset: LabeledMultiDataset[Int], trainSetRate: Double, jptimesFilePath: String, crossValidationTimes: Int): Unit = {

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
      val model = new LogisticRegressionWithLBFGS()
        .setNumClasses(classNum)
        .run(trainRdd)

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

  /**
    * Execute TFIDF ++ Word2Vec by Logistic Regression
    *
    * @param dataset
    * @param trainSetRate
    */
  def executeTFIDFAndWord2Vec(dataset: LabeledMultiDataset[Int], trainSetRate: Double, jptimesFilePath: String, word2VecDem: Int, crossValidationTimes: Int): Unit = {

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

      // word2vec model
      val word2VecModel = Word2VecGenerator.calcOrGetCacheModel(sparkContext = sparkContext, vectorSize = word2VecDem, jptimesFilePath = jptimesFilePath)
      // All zero vector
      val allZeroVec    = DenseVector.zeros[Double](word2VecDem)
      // all words for word2vec (Word2Vec ignore some words which don't appear frequently)
      val allWordsForWord2vec = word2VecModel.getVectors.keySet
      // word exist nor not
      def wordExist(word: Word): Boolean = allWordsForWord2vec.contains(word.entity)

      for{
        doc <- docs
        label = doc.label-1 // Why -1? Because Spark needs label 0 to (n-1) for n classification
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
      val model = new LogisticRegressionWithLBFGS()
        .setNumClasses(classNum)
        .run(trainRdd)

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

  }

  /**
    * Execute (Normalized TFIDF) ++ (Normalized Word2Vec) by Logistic Regression
    *
    * @param dataset
    * @param trainSetRate
    */
  def `execute (Normalized TFIDF) ++ (Normalized Word2Vec)`(dataset: LabeledMultiDataset[Int], trainSetRate: Double, word2VecDem: Int, crossValidationTimes: Int, enableNormalizationForTFIDF: Boolean, enableNormaliztionForWord2vec: Boolean): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)
    // Number of crossvalidation times > 0
    require(crossValidationTimes > 0)


    // extract the positive docs and negative docs
    val LabeledMultiDataset(docs, classNum) = dataset



    // Make a SparkContext
    val sparkContext = new SparkContext(new SparkConf().setAppName("JPTIMES").setMaster("local[*]"))


    // Get the feature vectors and all words containing all documents
    val (tfidfFeatureVectors, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWords(docs)

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
    val tfidfAbsMaxElem = tfidfFeatureVectors.values.map(vec => vec.maxBy(_.abs)).max
    println(s"IFIDF abx max: ${tfidfAbsMaxElem}")

    println(s"dem: ${tfidfFeatureVectors.head._2.length}")

    // Make exmaples (I think `Example` means labeled document(?))
    val labeledPoints: Seq[LabeledPoint] = {
      for{
        doc <- docs
        label = doc.label-1 // Why -1? Because Spark needs label 0 to (n-1) for n classification
        // TFIDF vector
        tfidfFeatures = new DenseVector[Double](tfidfFeatureVectors(doc))
        normalizedTfidfFeatures = tfidfFeatures  / tfidfAbsMaxElem
        // word2vec vector
        word2vec: DenseVector[Double] = docToAccumedWord2Vec(doc)
        // Normalized Word Vector
        normalizedWord2vecFeatures = word2vec / word2VecAbsMaxElem

        usedTfidfFeatures     = if(enableNormalizationForTFIDF) normalizedTfidfFeatures else tfidfFeatures
        usedWord2vecFeatures  = if(enableNormaliztionForWord2vec) normalizedWord2vecFeatures else word2vec
      } yield LabeledPoint(
        label = label.toDouble,
        features = new org.apache.spark.mllib.linalg.DenseVector(usedTfidfFeatures.toArray ++ usedWord2vecFeatures.toArray)
      )

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
      val model = new LogisticRegressionWithLBFGS()
        .setNumClasses(classNum)
        .run(trainRdd)

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
    println(s"Max Accuracy    : ${accuracies.max}")
    println(s"Min Accuracy    : ${accuracies.min}")
    println(s"-----------------------------------")
    println(s"SVM abx max     : ${tfidfAbsMaxElem}")
    println(s"word2vec abs max: ${word2VecAbsMaxElem}")
  }


  /**
    * Execute (Normalized TFIDF) ++ (Normalized Word2Vec) by Logistic Regression
    *
    * @param dataset
    * @param trainSetRate
    */
  def `execute train-test (Normalized TFIDF) ++ (Normalized Word2Vec)`(dataset: LabeledTrainTestMultiDataset[Int], trainSetRate: Double, word2VecDem: Int, word2VecNumIterations: Int, enableNormalizationForTFIDF: Boolean, enableNormaliztionForWord2vec: Boolean, additionalDocsForWord2vec: Seq[Document] = Seq.empty): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)
    //    // Number of crossvalidation times > 0
    //    require(crossValidationTimes > 0)


    // extract the positive docs and negative docs
    val LabeledTrainTestMultiDataset(docs, classNum) = dataset


    // Make a SparkContext
    val sparkContext = new SparkContext(new SparkConf().setAppName("JPTIMES").set("spark.executor.memory", "8g").set("spark.driver.memory", "8g").setMaster("local[*]"))



    // Split the vectors into vectors for train and vectors for test
    val (trainTestTrainDocs, trainTestTestDocs) =
    Rand.permutation(docs.length).draw.map(docs) // Shuffle the labeled docs
      .splitAt((docs.length * trainSetRate).toInt) // Split into train and test

    val trainDocs: Seq[LabeledDocument[Int]] = trainTestTrainDocs.map{d => LabeledDocument(d.label, d.trainDocument.entity)}
    val testDocs: Seq[LabeledDocument[Int]]  = trainTestTestDocs.map(d => LabeledDocument(d.label, d.testDocument.entity))
    val allDocs: Seq[LabeledDocument[Int]]   = trainDocs ++ testDocs

    // Get the feature vectors and all words containing all documents
    val (tfidfFeatureVectors, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWords(allDocs)

    println(s"number of words: ${allWords.size}")

    // Map for word to vector
    val wordToVec: Map[Word, DenseVector[Double]] = {
      // word2vec model
      //      val word2VecModel = Word2VecGenerator.calcOrGetCacheModel(sparkContext = sparkContext, vectorSize = word2VecDem, jptimesFilePath = jptimesFilePath)
      val word2VecModel = Word2VecGenerator.calcOrGetCacheModelByAllDocs(sparkContext = sparkContext, vectorSize = word2VecDem, numIterations = word2VecNumIterations, allDocuments = allDocs, additionalDocsForWord2vec = additionalDocsForWord2vec)
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
        doc <- allDocs
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
    def getLabeledPoints(docs: Seq[LabeledDocument[Int]]): Seq[LabeledPoint] = {
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
        LabeledPoint(
          label = label.toDouble,
          features = new org.apache.spark.mllib.linalg.DenseVector((usedTfidfFeatures ++ usedWord2vecFeatures).toArray)
        )
      }

    }


    val trainVecs: Seq[LabeledPoint] = getLabeledPoints(trainDocs)
    val testVecs: Seq[LabeledPoint]  = getLabeledPoints(testDocs)


    // Print the number of total and train and test
    println(s"Total: ${allDocs.length}, Train: ${trainVecs.length}, Test: ${testVecs.length}")


    val trainRdd = sparkContext.makeRDD(trainVecs)
    val testRdd = sparkContext.makeRDD(testVecs)


    println("Train starting...")

    // Run training algorithm to build the model
    val model = new LogisticRegressionWithLBFGS()
      .setNumClasses(classNum)
      .run(trainRdd)

    println("Train finished!")

    // Compute raw scores on the test set.
    val predictionAndLabels = testRdd.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }

    // Get evaluation metrics.
    val metrics = new MulticlassMetrics(predictionAndLabels)
    val accuracy = metrics.accuracy



    println(s"Accuracy    : ${accuracy}")
    println(s"-----------------------------------")
    println(s"TFIDF abx max     : ${tfidfAbsMaxElem}")
    println(s"word2vec abs max: ${word2VecAbsMaxElem}")
  }




  /**
    * Execute Spark's Normalized TFIDF) by Logistic Regression
    *
    * @param dataset
    * @param trainSetRate
    */
  def `execute (Normalized Spark's TFIDF)`(dataset: LabeledMultiDataset[Int], trainSetRate: Double, crossValidationTimes: Int): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)
    // Number of crossvalidation times > 0
    require(crossValidationTimes > 0)


    // extract the positive docs and negative docs
    val LabeledMultiDataset(docs, classNum) = dataset


    // Make a SparkContext
    val sparkContext = new SparkContext(new SparkConf().setAppName("JPTIMES").set("spark.executor.memory", "8g").set("spark.driver.memory", "8g").setMaster("local[*]"))


    // Get the feature vectors and all words containing all documents
    val (tfidfFeatureVectors, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWordsWithSparkContext(sparkContext, docs)

    println(s"number of words: ${allWords.size}")



    // Absolute-max element in all IFIDF vectors for normalization
    val tfidfAbsMaxElem = tfidfFeatureVectors.values.map(vec => vec.toArray.maxBy(_.abs)).max
    println(s"IFIDF abx max: ${tfidfAbsMaxElem}")

    // Make exmaples (I think `Example` means labeled document(?))
    val labeledPoints: Seq[LabeledPoint] = {
      for{
        doc <- docs
        label = doc.label-1 // Why -1? Because Spark needs label 0 to (n-1) for n classification
      } yield {
        // TFIDF vector
        val tfidfFeatures = tfidfFeatureVectors(doc)
        LabeledPoint(
          label = label.toDouble,
          features = tfidfFeatures
        )
      }

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
      val model = new LogisticRegressionWithLBFGS()
        .setNumClasses(classNum)
        .run(trainRdd)

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
    println(s"Max Accuracy    : ${accuracies.max}")
    println(s"Min Accuracy    : ${accuracies.min}")
    println(s"-----------------------------------")
    println(s"TFIDF abx max     : ${tfidfAbsMaxElem}")
  }

  /**
    * Execute (Normalized TFIDF) by Logistic Regression
    *
    * @param dataset
    * @param trainSetRate
    */
  def `execute train-test (Normalized TFIDF)`(dataset: LabeledTrainTestMultiDataset[Int], trainSetRate: Double, enableNormalizationForTFIDF: Boolean): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)
    //    // Number of crossvalidation times > 0
    //    require(crossValidationTimes > 0)


    // extract the positive docs and negative docs
    val LabeledTrainTestMultiDataset(docs, classNum) = dataset


    // Make a SparkContext
    val sparkContext = new SparkContext(new SparkConf().setAppName("JPTIMES").set("spark.executor.memory", "8g").set("spark.driver.memory", "8g").setMaster("local[*]"))



    // Split the vectors into vectors for train and vectors for test
    val (trainTestTrainDocs, trainTestTestDocs) =
    Rand.permutation(docs.length).draw.map(docs) // Shuffle the labeled docs
      .splitAt((docs.length * trainSetRate).toInt) // Split into train and test

    val trainDocs: Seq[LabeledDocument[Int]] = trainTestTrainDocs.map{d => LabeledDocument(d.label, d.trainDocument.entity)}
    val testDocs: Seq[LabeledDocument[Int]]  = trainTestTestDocs.map(d => LabeledDocument(d.label, d.testDocument.entity))
    val allDocs: Seq[LabeledDocument[Int]]   = trainDocs ++ testDocs

    // Get the feature vectors and all words containing all documents
    val (tfidfFeatureVectors, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWords(allDocs)

    println(s"number of words: ${allWords.size}")

    // Absolute-max element in all IFIDF vectors for normalization
    val tfidfAbsMaxElem = tfidfFeatureVectors.values.map(vec => vec.toArray.maxBy(_.abs)).max
    println(s"IFIDF abx max: ${tfidfAbsMaxElem}")

    // Make exmaples (I think `Example` means labeled document(?))
    def getLabeledPoints(docs: Seq[LabeledDocument[Int]]): Seq[LabeledPoint] = {
      for{
        doc <- docs
        label = doc.label-1 // Why -1? Because Spark needs label 0 to (n-1) for n classification
      } yield {
        // TFIDF vector
        val tfidfFeatures = tfidfFeatureVectors(doc)
        val tfidfFeatureStream = (0 until tfidfFeatures.size).toStream.map(i => tfidfFeatures(i))
        val normalizedTfidfFeatures = tfidfFeatureStream.map(_ / tfidfAbsMaxElem)

        val usedTfidfFeatures     = if(enableNormalizationForTFIDF) normalizedTfidfFeatures else tfidfFeatureStream
        LabeledPoint(
          label = label.toDouble,
          features = new org.apache.spark.mllib.linalg.DenseVector(usedTfidfFeatures.toArray)
        )
      }

    }


    val trainVecs: Seq[LabeledPoint] = getLabeledPoints(trainDocs)
    val testVecs: Seq[LabeledPoint]  = getLabeledPoints(testDocs)


    // Print the number of total and train and test
    println(s"Total: ${allDocs.length}, Train: ${trainVecs.length}, Test: ${testVecs.length}")


    val trainRdd = sparkContext.makeRDD(trainVecs)
    val testRdd = sparkContext.makeRDD(testVecs)


    println("Train starting...")

    // Run training algorithm to build the model
    val model = new LogisticRegressionWithLBFGS()
      .setNumClasses(classNum)
      .run(trainRdd)

    println("Train finished!")

    // Compute raw scores on the test set.
    val predictionAndLabels = testRdd.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }

    // Get evaluation metrics.
    val metrics = new MulticlassMetrics(predictionAndLabels)
    val accuracy = metrics.accuracy



    println(s"Accuracy    : ${accuracy}")
    println(s"-----------------------------------")
    println(s"TFIDF abx max     : ${tfidfAbsMaxElem}")
  }

  /**
    * Execute (Normalized TFIDF) ++ (Normalized Word2Vec) by Logistic Regression
    *
    * @param dataset
    * @param trainSetRate
    */
  def `execute (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(dataset: LabeledMultiDataset[Int], trainSetRate: Double, word2VecDem: Int, crossValidationTimes: Int, enableNormalizationForTFIDF: Boolean, enableNormaliztionForWord2vec: Boolean): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)
    // Number of crossvalidation times > 0
    require(crossValidationTimes > 0)


    // extract the positive docs and negative docs
    val LabeledMultiDataset(docs, classNum) = dataset


    // Make a SparkContext
    val sparkContext = new SparkContext(new SparkConf().setAppName("JPTIMES").set("spark.executor.memory", "8g").set("spark.driver.memory", "8g").setMaster("local[*]"))


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
    val labeledPoints: Seq[LabeledPoint] = {
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
        LabeledPoint(
          label = label.toDouble,
          features = new org.apache.spark.mllib.linalg.DenseVector((usedTfidfFeatures ++ usedWord2vecFeatures).toArray)
        )
      }

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
      val model = new LogisticRegressionWithLBFGS()
        .setNumClasses(classNum)
        .run(trainRdd)

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
    println(s"Max Accuracy    : ${accuracies.max}")
    println(s"Min Accuracy    : ${accuracies.min}")
    println(s"-----------------------------------")
    println(s"TFIDF abx max     : ${tfidfAbsMaxElem}")
    println(s"word2vec abs max: ${word2VecAbsMaxElem}")
  }


  /**
    * Execute (Normalized TFIDF) ++ (Normalized Word2Vec) by Logistic Regression
    *
    * @param dataset
    * @param trainSetRate
    */
  def `execute train-test (Normalized Spark's TFIDF) ++ (Normalized Word2Vec)`(dataset: LabeledTrainTestMultiDataset[Int], trainSetRate: Double, word2VecDem: Int, enableNormalizationForTFIDF: Boolean, enableNormaliztionForWord2vec: Boolean): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)
//    // Number of crossvalidation times > 0
//    require(crossValidationTimes > 0)


    // extract the positive docs and negative docs
    val LabeledTrainTestMultiDataset(docs, classNum) = dataset


    // Make a SparkContext
    val sparkContext = new SparkContext(new SparkConf().setAppName("JPTIMES").set("spark.executor.memory", "8g").set("spark.driver.memory", "8g").setMaster("local[*]"))



    // Split the vectors into vectors for train and vectors for test
    val (trainTestTrainDocs, trainTestTestDocs) =
    Rand.permutation(docs.length).draw.map(docs) // Shuffle the labeled docs
      .splitAt((docs.length * trainSetRate).toInt) // Split into train and test

    val trainDocs: Seq[LabeledDocument[Int]] = trainTestTrainDocs.map{d => LabeledDocument(d.label, d.trainDocument.entity)}
    val testDocs: Seq[LabeledDocument[Int]]  = trainTestTestDocs.map(d => LabeledDocument(d.label, d.testDocument.entity))
    val allDocs: Seq[LabeledDocument[Int]]   = trainDocs ++ testDocs

    // Get the feature vectors and all words containing all documents
    val (tfidfFeatureVectors, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWordsWithSparkContext(sparkContext, allDocs)

    println(s"number of words: ${allWords.size}")

    // Map for word to vector
    val wordToVec: Map[Word, DenseVector[Double]] = {
      // word2vec model
      //      val word2VecModel = Word2VecGenerator.calcOrGetCacheModel(sparkContext = sparkContext, vectorSize = word2VecDem, jptimesFilePath = jptimesFilePath)
      val word2VecModel = Word2VecGenerator.calcOrGetCacheModelByAllDocs(sparkContext = sparkContext, vectorSize = word2VecDem, numIterations = 1, allDocuments = allDocs)
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
        doc <- allDocs
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
    def getLabeledPoints(docs: Seq[LabeledDocument[Int]]): Seq[LabeledPoint] = {
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
        LabeledPoint(
          label = label.toDouble,
          features = new org.apache.spark.mllib.linalg.DenseVector((usedTfidfFeatures ++ usedWord2vecFeatures).toArray)
        )
      }

    }


    val trainVecs: Seq[LabeledPoint] = getLabeledPoints(trainDocs)
    val testVecs: Seq[LabeledPoint]  = getLabeledPoints(testDocs)


    // Print the number of total and train and test
    println(s"Total: ${allDocs.length}, Train: ${trainVecs.length}, Test: ${testVecs.length}")


    val trainRdd = sparkContext.makeRDD(trainVecs)
    val testRdd = sparkContext.makeRDD(testVecs)


    println("Train starting...")

    // Run training algorithm to build the model
    val model = new LogisticRegressionWithLBFGS()
      .setNumClasses(classNum)
      .run(trainRdd)

    println("Train finished!")

    // Compute raw scores on the test set.
    val predictionAndLabels = testRdd.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }

    // Get evaluation metrics.
    val metrics = new MulticlassMetrics(predictionAndLabels)
    val accuracy = metrics.accuracy



    println(s"Accuracy    : ${accuracy}")
    println(s"-----------------------------------")
    println(s"TFIDF abx max     : ${tfidfAbsMaxElem}")
    println(s"word2vec abs max: ${word2VecAbsMaxElem}")
  }



  /**
    * Execute (Normalized TFIDF) ++ (Normalized Word2Vec) by Logistic Regression
    *
    * @param dataset
    * @param trainSetRate
    */
  def `execute train-test (Normalized Spark's TFIDF)`(dataset: LabeledTrainTestMultiDataset[Int], trainSetRate: Double, enableNormalizationForTFIDF: Boolean): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)


    // extract the positive docs and negative docs
    val LabeledTrainTestMultiDataset(docs, classNum) = dataset


    // Make a SparkContext
    val sparkContext = new SparkContext(new SparkConf().setAppName("JPTIMES").set("spark.executor.memory", "8g").set("spark.driver.memory", "8g").setMaster("local[*]"))



    // Split the vectors into vectors for train and vectors for test
    val (trainTestTrainDocs, trainTestTestDocs) =
    Rand.permutation(docs.length).draw.map(docs) // Shuffle the labeled docs
      .splitAt((docs.length * trainSetRate).toInt) // Split into train and test

    val trainDocs: Seq[LabeledDocument[Int]] = trainTestTrainDocs.map{d => LabeledDocument(d.label, d.trainDocument.entity)}
    val testDocs: Seq[LabeledDocument[Int]]  = trainTestTestDocs.map(d => LabeledDocument(d.label, d.testDocument.entity))
    val allDocs: Seq[LabeledDocument[Int]]   = trainDocs ++ testDocs

    // Get the feature vectors and all words containing all documents
    val (tfidfFeatureVectors, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWordsWithSparkContext(sparkContext, allDocs)

    println(s"number of words: ${allWords.size}")


    // Absolute-max element in all IFIDF vectors for normalization
    val tfidfAbsMaxElem = tfidfFeatureVectors.values.map(vec => vec.toArray.maxBy(_.abs)).max
    println(s"IFIDF abx max: ${tfidfAbsMaxElem}")

    // Make exmaples (I think `Example` means labeled document(?))
    def getLabeledPoints(docs: Seq[LabeledDocument[Int]]): Seq[LabeledPoint] = {
      for{
        doc <- docs
        label = doc.label-1 // Why -1? Because Spark needs label 0 to (n-1) for n classification
      } yield {
        // TFIDF vector
        val tfidfFeatures = tfidfFeatureVectors(doc)
        val tfidfFeatureStream = (0 until tfidfFeatures.size).toStream.map(i => tfidfFeatures(i))
        val normalizedTfidfFeatures = tfidfFeatureStream.map(_ / tfidfAbsMaxElem)

        val usedTfidfFeatures     = if(enableNormalizationForTFIDF) normalizedTfidfFeatures else tfidfFeatureStream

        LabeledPoint(
          label = label.toDouble,
          features = new org.apache.spark.mllib.linalg.DenseVector(usedTfidfFeatures.toArray)
        )
      }

    }


    val trainVecs: Seq[LabeledPoint] = getLabeledPoints(trainDocs)
    val testVecs: Seq[LabeledPoint]  = getLabeledPoints(testDocs)


    // Print the number of total and train and test
    println(s"Total: ${allDocs.length}, Train: ${trainVecs.length}, Test: ${testVecs.length}")


    val trainRdd = sparkContext.makeRDD(trainVecs)
    val testRdd = sparkContext.makeRDD(testVecs)


    println("Train starting...")

    // Run training algorithm to build the model
    val model = new LogisticRegressionWithLBFGS()
      .setNumClasses(classNum)
      .run(trainRdd)

    println("Train finished!")

    // Compute raw scores on the test set.
    val predictionAndLabels = testRdd.map { case LabeledPoint(label, features) =>
      val prediction = model.predict(features)
      (prediction, label)
    }

    // Get evaluation metrics.
    val metrics = new MulticlassMetrics(predictionAndLabels)
    val accuracy = metrics.accuracy



    println(s"Accuracy    : ${accuracy}")
    println(s"-----------------------------------")
    println(s"TFIDF abx max     : ${tfidfAbsMaxElem}")
  }
}
