import breeze.linalg.{DenseVector, SparseVector}
import breeze.stats.distributions.Rand
import com.typesafe.scalalogging.log4j.Logging
import nak.classify.SVM
import nak.data.Example

/**
  * Created by Ryo on 2017/01/24.
  */
object NakSVMExecutor {
  /**
    * Generate a train-set file and a test-set file in SVM light Format
    * @param multiClassifiable
    * @param trainSetRate
    */
  def executeSVM(multiClassifiable: MultiClassifiable, trainSetRate: Double): Unit = {

    /** [[trainSetRate]] should be between 0.0 and 1.0 */
    require(0 <= trainSetRate && trainSetRate <= 1)


    // extract the positive docs and negative docs
    val MultiDataset(docsSeq) = multiClassifiable.multiDataset()

    // numbers of training sets
    val trainSetNumbers = docsSeq.map{docs => (docs.length * trainSetRate).toInt}


    // All documents (from a file)
    val engDocuments: Seq[EngDocument] = docsSeq.reduce(_ ++ _)

    // Get the feature vectors and all words containing all documents
    val (featureVectors, allWords) = FeatureVectorGeneratorE.generateTFIDFVectorsAndWords(engDocuments)

    println(s"number of words: ${allWords.size}")

    // Make exmaples (I think `Example` means labeled document(?))
    val examples: Seq[Example[Int, breeze.linalg.Vector[Double]]] = for{
      (docs, index) <- docsSeq.zipWithIndex
      label = index + 1
      doc <- docs
    } yield Example(label = label, features = SparseVector[Double](featureVectors(doc)))

    // Split the vectors into vectors for train and vectors for test
    val (trainVecs, testVecs) =
    Rand.permutation(examples.length).draw.map(examples) // Shuffle the vectors
      .splitAt((examples.length*trainSetRate).toInt)     // Split into train and test

    // Print the number of total and train and test
    println(s"Total: ${examples.length}, Train: ${trainVecs.length}, Test: ${testVecs.length}")


    println("Train starting...")
    val trainer = new SVM.SMOTrainer[Int, breeze.linalg.Vector[Double]](maxIterations = 1000) with Logging
    val classifier = trainer.train(trainVecs)
    println("Train finished!")


    for( ex <- testVecs) {
      val guessed = classifier.classify(ex.features)
      println(guessed,ex.label)
    }

    // Number of correct
    val correctNum = testVecs.count{ex =>
      val guessed = classifier.classify(ex.features)
      guessed == ex.label
    }

    // Print the accuracy
    println(s"Accuracy: ${correctNum.toDouble / testVecs.length}")

  }
}
