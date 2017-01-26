import java.io.File

import org.apache.spark.mllib.feature.{Word2Vec, Word2VecModel}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by Jimmy on 24/01/2017.
  */
object Word2VecGenerator {
  def calcOrGetCacheModel(sparkContext: SparkContext, vectorSize: Int, jptimesFilePath: String) = {

    /** [[jptimesFilePath]] file is required */
    require(new File(jptimesFilePath).exists())

    val corpusPath       = "./data/cache-word2vec-courpus"
    val modelPath        = "./data/cache-word2vec-model"

    if(!new File(corpusPath).exists()){
      sparkContext.textFile(jptimesFilePath).saveAsTextFile(corpusPath)
    }

    val input = sparkContext.textFile(corpusPath).map(_.split(" ").toVector)


    val model =
      if(new File(modelPath).exists()){
        Word2VecModel.load(sparkContext, modelPath)
      } else {
        val _model = new Word2Vec()
          .setVectorSize(vectorSize)
          //          .setNumPartitions(20)
          //          .setMinCount(1)
          .fit(input)

        _model.save(sparkContext, modelPath)
        _model
      }
    model
  }
}
