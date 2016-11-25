import java.io.{File, PrintWriter}

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
}
