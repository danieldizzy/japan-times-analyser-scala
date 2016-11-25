

/**
  * Created by Ryo Ota on 2016/11/07.
  */
object FeatureVectorGeneratorE {
  /**
    * Generate a map (Document -> IDIF)
    *
    * @param documents
    * @return
    */
  def generateTFIDFVectorsAndWords(documents: Seq[EngDocument]): (Map[EngDocument, Array[Double]], Set[Word]) = {

    // TF map
    val tfMap: Map[EngDocument, Map[Word, Int]] =
      documents.map(doc => (doc, doc.wordFreq)).toMap

    // All words in all documents
    val allWordsSet: Set[Word] =
      documents.map(_.wordsSet.toSet).reduce((a, b) => a ++ b)

    // DF Map
    val dfMap: Map[Word, Int] =
      allWordsSet.map { word =>
        (word, documents.count(_.wordsSet.contains(word)))
      }.toMap

    // Feature Vectors
    val featureVectors: Map[EngDocument, Array[Double]] =
      documents.map{doc =>
        val featureVector = allWordsSet.toList.map { word =>
          val N = documents.length
          tfMap(doc)(word) * Math.log(N.toDouble / dfMap(word))
        }.toArray

        (doc, featureVector)
      }.toMap


    // return a Feature Vectors and all words containing in all documents
    (featureVectors, allWordsSet)
  }
}
