import org.bson.types.BasicBSONList

import scala.util.{Failure, Success}

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
  def generateTFIDFVectorsAndWords(documents: Seq[Document]): (Map[Document, Array[Double]], Set[Word]) = {

    // TF map
    val tfMap: Map[Document, Map[Word, Int]] =
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
    val featureVectors: Map[Document, Array[Double]] = {
      val allWordList: List[Word] = allWordsSet.toList
      val N = documents.length

      documents.map { doc =>
        val featureVector = allWordList.map { word =>
          tfMap(doc)(word) * Math.log(N.toDouble / dfMap(word))
        }.toArray

        (doc, featureVector)
      }.toMap
    }


    // return a Feature Vectors and all words containing in all documents
    (featureVectors, allWordsSet)
  }

  /**
    * Generate a map (Document -> IDIF++word2vec)
    *
    * @param documents
    * @return
    */
  def `generate TFIDF & Word2Vec vectors and Words`(documents: Seq[Document]): (Map[Document, Array[Double]], Set[Word]) = {

    // TF map
    val tfMap: Map[Document, Map[Word, Int]] =
    documents.map(doc => (doc, doc.wordFreq)).toMap

    // All words in all documents
    val allWordsSet: Set[Word] =
    documents.map(_.wordsSet.toSet).reduce((a, b) => a ++ b)

    // DF Map
    val dfMap: Map[Word, Int] =
    allWordsSet.map { word =>
      (word, documents.count(_.wordsSet.contains(word)))
    }.toMap



    val pycall = new PythonCall()
    // Word2Vec Map
    val word2vec: Map[Word, Array[Double]] =
      allWordsSet.map{
        case (word@Word(wordStr)) =>
          val sendResult = pycall.send(funcName = "word2vec", wordStr)
          sendResult match {
            case Failure(exp) =>
              exp.printStackTrace()
              throw exp

            case _ => ()
          }
          val Success(bsonList : BasicBSONList) = sendResult
          // Convert bsonList to Array[Double]
          val featureVector: Array[Double] =
          (0 until bsonList.size())
            .map{idx => bsonList.get(idx).asInstanceOf[Double]}.toArray

          (word, featureVector)
      }.toMap
    pycall.close()
    println("AAAA")

    def elementsAdd(vectors: Seq[Array[Double]]): Array[Double] = {
      val accumed = vectors.head.clone()
      for(vec <- vectors.drop(1)){
        for((e, i) <- vec.zipWithIndex){
          accumed(i) = e
        }
      }
      accumed
    }

    // Feature Vectors
    val featureVectors: Map[Document, Array[Double]] =
    documents.map{doc =>
      val tfidfVec = allWordsSet.toList.map { word =>
        val N = documents.length
        tfMap(doc)(word) * Math.log(N.toDouble / dfMap(word))
      }.toArray


      val word2vecVec: Array[Double] = elementsAdd(doc.wordsSet.toList.map(word2vec))

      val featureVector: Array[Double] = tfidfVec ++ word2vecVec

      (doc, featureVector)
    }.toMap


    // return a Feature Vectors and all words containing in all documents
    (featureVectors, allWordsSet)
  }


  /**
    * Generate a map (Document -> IDIF++word2vec)
    *
    * @param documents
    * @return
    */
  def `generate TFIDF vec ++ (Word2Vec*TFIDF) vectors and Words`(documents: Seq[Document]): (Map[Document, Array[Double]], Set[Word]) = {

    // TF map
    val tfMap: Map[Document, Map[Word, Int]] =
    documents.map(doc => (doc, doc.wordFreq)).toMap

    // All words in all documents
    val allWordsSet: Set[Word] =
    documents.map(_.wordsSet.toSet).reduce((a, b) => a ++ b)

    // DF Map
    val dfMap: Map[Word, Int] =
    allWordsSet.map { word =>
      (word, documents.count(_.wordsSet.contains(word)))
    }.toMap



    val pycall = new PythonCall()
    // Word2Vec Map
    val word2vec: Map[Word, Array[Double]] =
    allWordsSet.map{
      case (word@Word(wordStr)) =>
        val Success(bsonList : BasicBSONList) = pycall.send(funcName = "word2vec", wordStr)
        // Convert bsonList to Array[Double]
        val featureVector: Array[Double] =
        (0 until bsonList.size())
          .map{idx => bsonList.get(idx).asInstanceOf[Double]}.toArray

        (word, featureVector)
    }.toMap
    pycall.close()
    println("AAAA")

    def elementsAdd(vectors: Seq[Array[Double]]): Array[Double] = {
      val accumed = vectors.head.clone()
      for(vec <- vectors.drop(1)){
        for((e, i) <- vec.zipWithIndex){
          accumed(i) = e
        }
      }
      accumed
    }


    // Feature Vectors
    val featureVectors: Map[Document, Array[Double]] =
    documents.map{doc =>
      val tfidfVec = allWordsSet.toList.map { word =>
        val N = documents.length
        tfMap(doc)(word) * Math.log(N.toDouble / dfMap(word))
      }.toArray


      val `(word2vec*tfidf)Vec`: Array[Double] = elementsAdd(doc.wordsSet.toList.map{word =>
        val N = documents.length
        val tfidf = tfMap(doc)(word) * Math.log(N.toDouble / dfMap(word))
        word2vec(word).map(_ * tfidf)
      })

      val featureVector: Array[Double] = tfidfVec ++ `(word2vec*tfidf)Vec`

      (doc, featureVector)
    }.toMap


    // return a Feature Vectors and all words containing in all documents
    (featureVectors, allWordsSet)
  }
}
