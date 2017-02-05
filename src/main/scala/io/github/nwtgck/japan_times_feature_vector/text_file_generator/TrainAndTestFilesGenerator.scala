package io.github.nwtgck.japan_times_feature_vector.text_file_generator

import java.io.{File, PrintWriter}

import io.github.nwtgck.japan_times_feature_vector.datatype._
import io.github.nwtgck.japan_times_feature_vector.vector_generator.FeatureVectorGeneratorE

/**
  * A training-set file and test-set file generator
  */
object TrainAndTestFilesGenerator {

  /**
    * Generate a train-set file and a test-set file in SVM light Format
    * @param binaryClassifiable
    * @param trainFilePath
    * @param testFilePath
    * @param trainSetRate
    */
  def generateSvmLightFormatFiles(binaryClassifiable: BinaryClassifiable, trainFilePath: String, testFilePath: String, trainSetRate: Double): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)


    // labels for dataset
    val posLabel    = "+1"
    val negLabel    = "-1"

    // extract the positive docs and negative docs
    val BinaryDataset(posDocs, negDocs) = binaryClassifiable.binaryDataset()

    // number of training sets
    val posTrainSetNumber = (posDocs.length * trainSetRate).toInt
    val negTrainSetNumber = (negDocs.length * trainSetRate).toInt

    // create PrintWriters for train-set file and test-set file
    val trainFileWriter      = new PrintWriter(new File(trainFilePath))
    val testFileWriter      =  new PrintWriter(new File(testFilePath))

    // All documents (from a file)
    val engDocuments: Seq[EngDocument] = posDocs ++ negDocs

    // Get the feature vectors and all words containing all documents
    val (featureVectors, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWords(engDocuments)

    println(s"number of words: ${allWords.size}")


    /**
      * Transform a feature vector into a SVM Light format String
      *
      * example)
      *
      * +1 1:0.1 91:0.5 .....
      *
      * @param label positive or negative
      * @param featureVec a feature vector
      * @return
      *
      */
    def figureVectorToSvmLightFormat(label: String, featureVec: Array[Double]): String = {
      label + " " + featureVec.zipWithIndex.filter{case (e, i) => e != 0}.map{case (e, i) => s"${i+1}:${e}"}.mkString(" ")
    }

    // make train file
    for(posDoc <- posDocs.take(posTrainSetNumber)){
      trainFileWriter.println(figureVectorToSvmLightFormat(posLabel, featureVectors(posDoc)))
    }

    for(negDoc <- negDocs.take(negTrainSetNumber)){
      trainFileWriter.println(figureVectorToSvmLightFormat(negLabel, featureVectors(negDoc)))
    }

    // make test file
    for(posDoc <- posDocs.drop(posTrainSetNumber)){
      testFileWriter.println(figureVectorToSvmLightFormat(posLabel, featureVectors(posDoc)))
    }

    for(negDoc <- negDocs.drop(negTrainSetNumber)){
      testFileWriter.println(figureVectorToSvmLightFormat(negLabel, featureVectors(negDoc)))
    }

    trainFileWriter.close()
    testFileWriter.close()
  }



  /**
    * Generate a train-set file and a test-set file in SVM light Format
    * @param multiClassifiable
    * @param trainFilePath
    * @param testFilePath
    * @param trainSetRate
    */
  def generateMultiSvmLightFormatFiles(multiClassifiable: MultiClassifiable, trainFilePath: String, testFilePath: String, trainSetRate: Double): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)


    // extract the positive docs and negative docs
    val MultiDataset(docsSeq) = multiClassifiable.multiDataset()

    // numbers of training sets
    val trainSetNumbers = docsSeq.map{docs => (docs.length * trainSetRate).toInt}


    // create PrintWriters for train-set file and test-set file
    val trainFileWriter      = new PrintWriter(new File(trainFilePath))
    val testFileWriter      =  new PrintWriter(new File(testFilePath))

    // All documents (from a file)
    val engDocuments: Seq[EngDocument] = docsSeq.reduce(_ ++ _)

    // Get the feature vectors and all words containing all documents
    val (featureVectors, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWords(engDocuments)

    println(s"number of words: ${allWords.size}")


    /**
      * Transform a feature vector into a SVM Light format String
      *
      * example)
      *
      * +1 1:0.1 91:0.5 .....
      *
      * @param label positive or negative
      * @param featureVec a feature vector
      * @return
      *
      */
    def featureVectorToSvmLightFormat(label: String, featureVec: Array[Double]): String = {
      label + " " + featureVec.zipWithIndex.filter{case (e, i) => e != 0}.map{case (e, i) => s"${i+1}:${e}"}.mkString(" ")
    }

    // make train file
    docsSeq.zip(trainSetNumbers).map{case (docs, trainSetNumber) =>
      // split docs into (trainSet, testSet)
      docs.splitAt(trainSetNumber)
    }.zipWithIndex.foreach{case ((trains, tests), index) =>
      // write docs to the train-set file
      for(doc <- trains)
        trainFileWriter.println(featureVectorToSvmLightFormat(label=index+1+"", featureVectors(doc)))
      // write docs to the test-set file
      for(doc <- tests)
        testFileWriter.println(featureVectorToSvmLightFormat(label=index+1+"", featureVectors(doc)))
    }

    trainFileWriter.close()
    testFileWriter.close()
  }



  /**
    * Generate a train-set file and a test-set file in SVM light Format
    * @param multiClassifiable
    * @param trainFilePath
    * @param testFilePath
    * @param trainSetRate
    */
  def generateTrainTestMultiSvmLightFormatFiles(multiClassifiable: TrainTestMultiClassifiable, trainFilePath: String, testFilePath: String, trainSetRate: Double): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)


    // extract the positive docs and negative docs
    val TrainTestMultiDataset(trainTestDocsSeq) = multiClassifiable.trainTestMultiDataset()

    // numbers of training sets
    val trainSetNumbers = trainTestDocsSeq.map{docs => (docs.length * trainSetRate).toInt}


    // create PrintWriters for train-set file and test-set file
    val trainFileWriter      = new PrintWriter(new File(trainFilePath))
    val testFileWriter      =  new PrintWriter(new File(testFilePath))

    // All documents (from a file)
    val engDocuments: Seq[EngDocument] = trainTestDocsSeq.zip(trainSetNumbers).flatMap{
      case (trainTestDocs, trainSetNum) =>
        val trainDocs = trainTestDocs.take(trainSetNum).map(_.trainDocument)
        val testDocs  = trainTestDocs.drop(trainSetNum).map(_.testDocument)
        trainDocs ++ testDocs
    }

    // Get the feature vectors and all words containing all documents
    val (featureVectors, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWords(engDocuments)

    println(s"number of words: ${allWords.size}")


    /**
      * Transform a feature vector into a SVM Light format String
      *
      * example)
      *
      * +1 1:0.1 91:0.5 .....
      *
      * @param label positive or negative
      * @param featureVec a feature vector
      * @return
      *
      */
    def featureVectorToSvmLightFormat(label: String, featureVec: Array[Double]): String = {
      label + " " + featureVec.zipWithIndex.filter{case (e, i) => e != 0}.map{case (e, i) => s"${i+1}:${e}"}.mkString(" ")
    }

    // make a train file and a test file
    trainTestDocsSeq.zip(trainSetNumbers).map{case (docs, trainSetNumber) =>
      // split docs into (trainSet, testSet)
      val (trainSet, testSet) = docs.splitAt(trainSetNumber)
      (trainSet.map(_.trainDocument), testSet.map(_.testDocument))
    }.zipWithIndex.foreach{case ((trains, tests), index) =>
      // write docs to the train-set file
      for(doc <- trains)
        trainFileWriter.println(featureVectorToSvmLightFormat(label=index+1+"", featureVectors(doc)))
      // write docs to the test-set file
      for(doc <- tests)
        testFileWriter.println(featureVectorToSvmLightFormat(label=index+1+"", featureVectors(doc)))
    }

    trainFileWriter.close()
    testFileWriter.close()
  }

  /**
    * Generate a train-set file and a test-set file in SVM light Format with a generator
    * @param multiClassifiable
    * @param trainFilePath
    * @param testFilePath
    * @param trainSetRate
    */
  def generateMultiSvmLightFormatFilesWithGenerator(multiClassifiable: MultiClassifiable, trainFilePath: String, testFilePath: String, trainSetRate: Double, generator: (Seq[Document] => (Map[Document, Array[Double]], Set[Word]))): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)


    // extract the positive docs and negative docs
    val MultiDataset(docsSeq) = multiClassifiable.multiDataset()

    // numbers of training sets
    val trainSetNumbers = docsSeq.map{docs => (docs.length * trainSetRate).toInt}


    // create PrintWriters for train-set file and test-set file
    val trainFileWriter      = new PrintWriter(new File(trainFilePath))
    val testFileWriter      =  new PrintWriter(new File(testFilePath))

    // All documents (from a file)
    val engDocuments: Seq[EngDocument] = docsSeq.reduce(_ ++ _)

    // Get the feature vectors and all words containing all documents
    val (featureVectors, allWords) = generator(engDocuments)

    println(s"number of words: ${allWords.size}")


    /**
      * Transform a feature vector into a SVM Light format String
      *
      * example)
      *
      * +1 1:0.1 91:0.5 .....
      *
      * @param label positive or negative
      * @param featureVec a feature vector
      * @return
      *
      */
    def featureVectorToSvmLightFormat(label: String, featureVec: Array[Double]): String = {
      label + " " + featureVec.zipWithIndex.filter{case (e, i) => e != 0}.map{case (e, i) => s"${i+1}:${e}"}.mkString(" ")
    }

    // make train file
    docsSeq.zip(trainSetNumbers).map{case (docs, trainSetNumber) =>
      // split docs into (trainSet, testSet)
      docs.splitAt(trainSetNumber)
    }.zipWithIndex.foreach{case ((trains, tests), index) =>
      // write docs to the train-set file
      for(doc <- trains)
        trainFileWriter.println(featureVectorToSvmLightFormat(label=index+1+"", featureVectors(doc)))
      // write docs to the test-set file
      for(doc <- tests)
        testFileWriter.println(featureVectorToSvmLightFormat(label=index+1+"", featureVectors(doc)))
    }

    trainFileWriter.close()
    testFileWriter.close()
  }


  /**
    * Generate a train-set file and a test-set file with generator in SVM light Format
    * @param multiClassifiable
    * @param trainFilePath
    * @param testFilePath
    * @param trainSetRate
    */
  def generateTrainTestMultiSvmLightFormatFilesWithGenerator(multiClassifiable: TrainTestMultiClassifiable, trainFilePath: String, testFilePath: String, trainSetRate: Double, generator: (Seq[Document] => (Map[Document, Array[Double]], Set[Word]))): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)


    // extract the positive docs and negative docs
    val TrainTestMultiDataset(trainTestDocsSeq) = multiClassifiable.trainTestMultiDataset()

    // numbers of training sets
    val trainSetNumbers = trainTestDocsSeq.map{docs => (docs.length * trainSetRate).toInt}


    // create PrintWriters for train-set file and test-set file
    val trainFileWriter      = new PrintWriter(new File(trainFilePath))
    val testFileWriter      =  new PrintWriter(new File(testFilePath))

    // All documents (from a file)
    val engDocuments: Seq[EngDocument] = trainTestDocsSeq.zip(trainSetNumbers).flatMap{
      case (trainTestDocs, trainSetNum) =>
        val trainDocs = trainTestDocs.take(trainSetNum).map(_.trainDocument)
        val testDocs  = trainTestDocs.drop(trainSetNum).map(_.testDocument)
        trainDocs ++ testDocs
    }

    // Get the feature vectors and all words containing all documents
    val (featureVectors, allWords) = generator(engDocuments)

    println(s"number of words: ${allWords.size}")


    /**
      * Transform a feature vector into a SVM Light format String
      *
      * example)
      *
      * +1 1:0.1 91:0.5 .....
      *
      * @param label positive or negative
      * @param featureVec a feature vector
      * @return
      *
      */
    def featureVectorToSvmLightFormat(label: String, featureVec: Array[Double]): String = {
      label + " " + featureVec.zipWithIndex.filter{case (e, i) => e != 0}.map{case (e, i) => s"${i+1}:${e}"}.mkString(" ")
    }

    // make a train file and a test file
    trainTestDocsSeq.zip(trainSetNumbers).map{case (docs, trainSetNumber) =>
      // split docs into (trainSet, testSet)
      val (trainSet, testSet) = docs.splitAt(trainSetNumber)
      (trainSet.map(_.trainDocument), testSet.map(_.testDocument))
    }.zipWithIndex.foreach{case ((trains, tests), index) =>
      // write docs to the train-set file
      for(doc <- trains)
        trainFileWriter.println(featureVectorToSvmLightFormat(label=index+1+"", featureVectors(doc)))
      // write docs to the test-set file
      for(doc <- tests)
        testFileWriter.println(featureVectorToSvmLightFormat(label=index+1+"", featureVectors(doc)))
    }

    trainFileWriter.close()
    testFileWriter.close()
  }
}
