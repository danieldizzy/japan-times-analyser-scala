package io.github.nwtgck.japan_times_feature_vector

/**
  * Created by Jimmy on 2016/11/14.
  */
object Similarity {

  /**
    * @param vector
    * @return size of a vector
    */
  private def sizeOfVector(vector: Array[Double]): Double =
    Math.sqrt(vector.map(e => e * e).sum)

  /**
    *
    * @param vec1
    * @param vec2
    * @return
    */
  private def dotTwoVectors(vec1: Array[Double], vec2: Array[Double]): Double = {
    vec1.zip(vec2).map{case (e1, e2) => e1 * e2}.sum
  }

  /**
    * @param featureVec1
    * @param featureVec2
    * @return cosine similarity of given 2 feature vectors
    */
  def cosSimilarity(featureVec1: Array[Double], featureVec2: Array[Double]): Double = {
    dotTwoVectors(featureVec1, featureVec2) / (sizeOfVector(featureVec1) * sizeOfVector(featureVec2))
  }
}
